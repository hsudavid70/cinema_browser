package com.skynet.dwhsu.cinema_browser.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.skynet.dwhsu.cinema_browser.sync.MovieSyncAdapter;

/**
 *   Fragment containing grid of movie posters using recyclerview grid layout
 *
 */

public class MovieRecyclerGridFragment extends Fragment implements  SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MovieGridFragment";
    private static final int POPULAR_LOADER = 0;
    private static final int TOP_RATED_LOADER = 1;
    private static final int FAVOURITE_LOADER = 2;
    protected RecyclerView mRecyclerView;
    protected ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView.LayoutManager mLayoutManager;
    private static int currentLoad = POPULAR_LOADER;
    private boolean mTwoPane=false;
    private boolean isRefreshing = false;
    private static final String POSITION_KEY = "selected position";
    private static final String VIEW_KEY = "selected view";
    private static final String sMovieFavouriteSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COL_FAVOURITE+ " = 1 ";

    private static final String[] GRID_VIEW_COLUMNS = {
            MovieContract.MovieEntry.COL_MOVIE_ID,
            MovieContract.MovieEntry.COL_POSTER_PATH,
            MovieContract.MovieEntry.COL_RATING,
            MovieContract.MovieEntry.COL_POPULARITY
    };

    private MovieRecyclerCursorAdapter mMovieRecyclerGridAdapter;
    public MovieRecyclerGridFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        // handle menu events
        mTwoPane = getArguments().getBoolean("twoPane");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        int grid_width = 0;
        View rootView = inflater.inflate(R.layout.fragment_movie_recycler_grid, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        if( mTwoPane ){
            Log.d(TAG,"grid width 3");
            grid_width = 3;
        }
        else{
            Log.d(TAG,"grid width 2");
            grid_width = 2;
        }

        //mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclergridview);
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(),
                new RecyclerViewItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View v, int position) {
                        //TODO:
                        String movie_id = Integer.toString(v.getId());
                        String movie_link = (String)v.getTag();
                        //v.setSelected(true);
                        if(mTwoPane){
                            Fragment fragment = new MovieDetailFragment();
                            Bundle args = new Bundle();
                            args.putBoolean("hasData",true);
                            args.putBoolean("twoPane",true);
                            args.putString("movieId",movie_id);
                            args.putString("movieLink",movie_link);
                            fragment.setArguments(args);
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = fm.beginTransaction();
                            transaction.replace(R.id.movie_detail_container, fragment);
                            transaction.commit();
                        }
                        else {
                            Intent intent = new Intent(getContext(), MovieDetailActivity.class)
                                    .putExtra(Intent.EXTRA_TEXT, movie_link).putExtra(Intent.EXTRA_TITLE, movie_id);
                            startActivity(intent);
                        }
                    }
                }));
        mMovieRecyclerGridAdapter = new MovieRecyclerCursorAdapter(getContext(), null);



        // override method for grid layout progress bar
        mLayoutManager = new GridLayoutManager(getContext(),getNumberOfColumns()){
           // mLayoutManager = new GridLayoutManager(getContext(),grid_width){
            /*
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                //TODO if the items are filtered, considered hiding the fast scroller here
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1)
                        //not initialized, or no items shown, so show progress circle
                        mProgressBar.setVisibility(View.VISIBLE);
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                //if some items are displayed remove progress circle
                mProgressBar.setVisibility(itemsShown > 0? View.GONE : View.VISIBLE);
            }
            */
        };

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mMovieRecyclerGridAdapter);
        mRecyclerView.setHasFixedSize(true);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("POSITION") ){
                Log.d(TAG,"[onCreateView] restore SCROLL state");
                mLayoutManager.scrollToPosition(savedInstanceState.getInt("POSITION"));
            }
            if(savedInstanceState.containsKey(VIEW_KEY) ){
                currentLoad = savedInstanceState.getInt(VIEW_KEY);
                Log.d(TAG,"[onCreateView] restore KEY: "+currentLoad);
            }

        }

        mSwipeRefreshLayout.setOnRefreshListener(this);
        //mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        return rootView;
    }

    @Override
    public void onRefresh() {
        Log.d(TAG,"onRefresh");
        if(!isRefreshing) {

            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting()) {

                isRefreshing = true;
                MovieSyncAdapter.syncImmediately(getActivity());
            }
            else{
                Toast.makeText(getActivity(),"Check network connection",Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    public void stopRefreshing() {
        Log.d(TAG,"stopRefreshing");
        if(isRefreshing) {
            mSwipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG,"[onActivityCreated]");

        if(savedInstanceState == null) {
            Log.d(TAG,"[onActivityCreated] no savedInstance");
            // load popular movies initially
            //getLoaderManager().initLoader(POPULAR_LOADER, null, this);
            //getLoaderManager().initLoader(TOP_RATED_LOADER, null, this);
        }
        else{
            Log.d(TAG,"[onActivityCreated] saved instance found");
            //getLoaderManager().restartLoader(currentLoad, null, this);
        }

        getLoaderManager().restartLoader(currentLoad, null, this);
        super.onActivityCreated(savedInstanceState);
    }
/*

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_grid, menu);

    }
*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_popular: {
                Toast.makeText(getContext(), "Showing most popular", Toast.LENGTH_SHORT).show();
                currentLoad = POPULAR_LOADER;
                getLoaderManager().restartLoader(POPULAR_LOADER, null, this);

                return true;
            }
            case R.id.action_top_rated: {
                Toast.makeText(getContext(), "Showing top rated", Toast.LENGTH_SHORT).show();
                currentLoad = TOP_RATED_LOADER;
                getLoaderManager().restartLoader(TOP_RATED_LOADER, null, this);

                return true;
            }
            case R.id.action_favourite: {
                Toast.makeText(getContext(), "Showing favourite", Toast.LENGTH_SHORT).show();
                currentLoad = FAVOURITE_LOADER;
                getLoaderManager().restartLoader(FAVOURITE_LOADER, null, this);

                return true;
            }
            case R.id.action_refresh: {
                Toast.makeText(getContext(), "Getting latest movies", Toast.LENGTH_SHORT).show();
                MovieSyncAdapter.syncImmediately(getActivity());
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstance");
        int position = ((GridLayoutManager)mLayoutManager).findFirstVisibleItemPosition();
        if(position != ((GridLayoutManager)mLayoutManager).INVALID_OFFSET ) {
            outState.putInt("POSITION", position);
        }
        //outState.putParcelable("GRID_STATE",   mLayoutManager.onSaveInstanceState());
        outState.putInt(VIEW_KEY, currentLoad);
        super.onSaveInstanceState(outState);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.d(TAG,"onCreateLoader "+id);
        //mProgressBar.setVisibility(View.VISIBLE);
        // Sort order:  Ascending, by date.
        String ord = new String();
        String sel = null;
        String[] selArgs = null;
        switch(id) {
            case POPULAR_LOADER:
                ord = "popularity DESC";
                break;
            case TOP_RATED_LOADER:
                ord = "vote_average DESC";
                break;
            case FAVOURITE_LOADER:
                ord = "popularity DESC";
                sel = sMovieFavouriteSelection;
                //selArgs = new String[]{"1"};
                break;
            default:
                break;
        }
        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                GRID_VIEW_COLUMNS,
                sel,
                null,
                ord);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(TAG,"onLoadFinished "+currentLoad);
        stopRefreshing();
        if(cursorLoader.getId() == currentLoad) {
            Log.d(TAG,"onLoadFinished swap cursor");
            mMovieRecyclerGridAdapter.swapCursor(cursor);
            //mProgressBar.setVisibility(View.GONE);

        }
        /*
        else{
            getLoaderManager().restartLoader(currentLoad,null,this);
        }
        */
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG,"onLoaderReset");

        if(cursorLoader.getId() == currentLoad) {
        mMovieRecyclerGridAdapter.swapCursor(null);
        }
    }

    public int getNumberOfColumns() {
        // Get screen width
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float widthTotalPx = displayMetrics.widthPixels;
        if (mTwoPane) {
            widthTotalPx = widthTotalPx / 2;
        }
        // Calculate desired width
            Log.d(TAG,"make grid columns");
            float desiredPx = getResources().getDimensionPixelSize(R.dimen.grid_item_width);
            int columns = Math.round(widthTotalPx / desiredPx);
            return columns > 2 ? columns : 2;
    }


}
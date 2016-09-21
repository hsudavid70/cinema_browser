package com.skynet.dwhsu.cinema_browser.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skynet.dwhsu.cinema_browser.MovieApp;
import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.skynet.dwhsu.cinema_browser.ui.simpledecoration.linear.DividerItemDecoration;
import com.skynet.dwhsu.cinema_browser.ui.simpledecoration.linear.StartOffsetItemDecoration;
import com.squareup.picasso.Picasso;


/**
 * Fragment of movie detail information
 *
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,AdapterView.OnItemClickListener {
    private static final String TAG = "MovieDetailFragment";
    private static boolean mHasData;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    private ExtraRecyclerCursorAdapter mExtraCursorAdapter;
    private RecyclerView.ItemDecoration mStartOffsetItemDecoration;
    private RecyclerView.ItemDecoration mDividerItemDecoration;
    private boolean mTwoPane = false;
    private static int mFavourite = -1;
    private static String mMovieId;
    private static String mMovieLink;
    private static TextView mRating;
    private static TextView mOverview;
    private static TextView mReleaseDate;
    private static TextView mTitle;
    private static ImageView mImageView;
    private static Button mFavouriteButton;
    private static final int MOVIE_EXTRA_LOADER = 1;
    private static final int MOVIE_DETAIL_LOADER = 2;
    private static final String[] MOVIE_DETAIL_COLUMNS = {
            MovieContract.MovieEntry.COL_MOVIE_ID,
            MovieContract.MovieEntry.COL_ORIG_TITLE,
            MovieContract.MovieEntry.COL_RATING,
            MovieContract.MovieEntry.COL_RELEASE_DATE,
            MovieContract.MovieEntry.COL_OVERVIEW,
            MovieContract.MovieEntry.COL_FAVOURITE
    };
    private static final String[] MOVIE_EXTRA_COLUMNS = {"*"};
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // handle menu events
        Log.d(TAG,"onCreate");
        if(getArguments().containsKey("twoPane")){
            Log.d(TAG,"[onCreate] set twoPane fragment");
            mTwoPane = getArguments().getBoolean("twoPane");
        }
        mHasData = getArguments().getBoolean("hasData");

        if(mHasData) {
            mMovieId = getArguments().getString("movieId");
            mMovieLink = getArguments().getString("movieLink");
        }
        //Toast.makeText(getActivity(),mMovieId,Toast.LENGTH_SHORT).show();
        setHasOptionsMenu(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if(mHasData) {
            rootView = inflater.inflate(R.layout.movie_detail_fragment, container, false);
            mOverview = (TextView) rootView.findViewById((R.id.movie_overview));
            mRating = (TextView) rootView.findViewById((R.id.movie_rating));
            mImageView = (ImageView) rootView.findViewById(R.id.movie_image);
            mReleaseDate = (TextView) rootView.findViewById(R.id.movie_date);
            mTitle = (TextView) rootView.findViewById(R.id.movie_title);

            //mFavouriteButton = (Button) rootView.findViewById(R.id.movie_button);
            //TODO set button text from provider here?
            /*
            mFavouriteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    //Toast.makeText(getContext(),"Press",Toast.LENGTH_SHORT).show();
                    // skip processing until initialized
                    //mFavouriteButton.setText("Unloaded");
                    if (mFavourite != -1) {
                        if (mFavourite == 0) {
                            //mFavourite = 1;
                            //mFavouriteButton.setText("Unmark favourite");
                            ContentValues vals = new ContentValues();
                            vals.put(MovieContract.MovieEntry.COL_FAVOURITE, 1);
                            getContext().getContentResolver().update(MovieContract.MovieEntry.buildMovieDetailUri(mMovieId), vals, null, null);
                        } else {
                            //mFavourite = 0;
                            ContentValues vals = new ContentValues();
                            vals.put(MovieContract.MovieEntry.COL_FAVOURITE, 0);
                            getContext().getContentResolver().update(MovieContract.MovieEntry.buildMovieDetailUri(mMovieId), vals, null, null);
                            //mFavouriteButton.setText("Mark favourite");
                        }
                    }
                    // modify content provider

                }
            });*/

            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.extra_recyclerview);
            mExtraCursorAdapter = new ExtraRecyclerCursorAdapter(getContext(), null);
            mLayoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            /*
            mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(),
                    new RecyclerViewItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View v, int position) {
                            if(v.getTag()!= null && v.getTag().equals("IMAGE")){
                                Log.d(TAG,"image clicked");
                            }
                            Log.d(TAG, "position:" + position);
                            TextView bottomText = (TextView) v.findViewById(R.id.list_item_extra_textview_bottom);
                            String type = (String) bottomText.getTag();
                            String link = (String) bottomText.getText();
                            Log.d(TAG, type);
                            Log.d(TAG, link);
                            if (type.equals("trailer")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + link));
                                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(getActivity(), "Youtube app not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                                startActivity(browserIntent);
                                if (browserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                    startActivity(browserIntent);
                                } else {
                                    Toast.makeText(getActivity(), "Browser app not found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }));*/

            mRecyclerView.setAdapter(mExtraCursorAdapter);

            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_basic);
            Drawable startDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.start_offset);
            mStartOffsetItemDecoration = new StartOffsetItemDecoration(startDrawable);
            mDividerItemDecoration = new DividerItemDecoration(dividerDrawable);
            mRecyclerView.addItemDecoration(mStartOffsetItemDecoration);
            mRecyclerView.addItemDecoration(mDividerItemDecoration);

            //Glide.with(getContext()).load(MovieApp.DEFAULT_POSTER_URL+imageLink).placeholder(R.drawable.default_empty).error(R.drawable.empty_image).into(mImageView);
            Picasso.with(getContext()).load(MovieApp.DEFAULT_POSTER_URL+mMovieLink).error(R.drawable.default_empty).into(mImageView);
        }
        else{
            rootView = inflater.inflate(R.layout.empty_detail_fragment, container, false);

        }
            return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if(mHasData) {
            getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
            getLoaderManager().initLoader(MOVIE_EXTRA_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    //TODO add review clicks here
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG,"[onCreateOptionsMenu]");
        super.onCreateOptionsMenu(menu, inflater);
        if(!mTwoPane) {
            MenuItem item = menu.findItem(R.id.fav_mark);
            item.setIcon(mFavourite == 1 ? R.drawable.ic_favorite_border_white_24dp : R.drawable.ic_favorite_white_24dp);
        }
        else{
                MenuItem item = menu.findItem(R.id.fav_mark);
                item.setVisible(true);
        }
        //mMenu = menu;
        //inflater.inflate(R.menu.menu_grid, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        Log.d(TAG,"[onPrepareOptionsMenu]");
        //if(!mTwoPane) {
            MenuItem item = menu.findItem(R.id.fav_mark);
            item.setIcon(mFavourite == 1 ? R.drawable.ic_favorite_border_white_24dp : R.drawable.ic_favorite_white_24dp);
       // }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        switch(id) {
            case R.id.fav_mark: {


                if (mFavourite != -1) {
                    if (mFavourite == 0) {
                        ContentValues vals = new ContentValues();
                        vals.put(MovieContract.MovieEntry.COL_FAVOURITE, 1);
                        getContext().getContentResolver().update(MovieContract.MovieEntry.buildMovieDetailUri(mMovieId), vals, null, null);
                        Toast.makeText(getActivity(),"Saved to favourites",Toast.LENGTH_SHORT).show();

                    } else {
                        ContentValues vals = new ContentValues();
                        vals.put(MovieContract.MovieEntry.COL_FAVOURITE, 0);
                        getContext().getContentResolver().update(MovieContract.MovieEntry.buildMovieDetailUri(mMovieId), vals, null, null);
                        Toast.makeText(getActivity(),"Removed from favourites",Toast.LENGTH_SHORT).show();
                    }

                }


                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Log.d(TAG,"onCreateLoader");

        Uri loadURI=null;
        String proj[] = null;
        switch(id) {
            case MOVIE_DETAIL_LOADER:
                loadURI = MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(mMovieId).build();
                proj = MOVIE_DETAIL_COLUMNS;
                break;
            case MOVIE_EXTRA_LOADER:
                loadURI = MovieContract.MovieExtraEntry.CONTENT_URI.buildUpon().appendPath(mMovieId).build();
                proj = MOVIE_EXTRA_COLUMNS;
                break;
            default:
                break;
        }
        return new CursorLoader(getActivity(),
                loadURI,
                proj,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(cursor.getCount()>0) {
            switch(cursorLoader.getId()) {
                case MOVIE_DETAIL_LOADER: {
                    cursor.moveToFirst();
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_ORIG_TITLE));
                    Log.d(TAG, "onLoadFinished " + title);
                    //Toast.makeText(getActivity(),title,Toast.LENGTH_SHORT).show();
                    mOverview.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_OVERVIEW)));
                    //mRating.setText("Rating: "+cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_RATING))+"/10");
                    String ratingFormat = getResources().getString(R.string.format_rating);
                    String ratingText = String.format(ratingFormat, cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_RATING)));
                    mRating.setText(ratingText);
                    String releaseFormat = getResources().getString(R.string.format_release_date);
                    String releaseText = String.format(releaseFormat, cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_RELEASE_DATE)));
                    mReleaseDate.setText(releaseText);
                    mTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_ORIG_TITLE)));
                    mFavourite = cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_FAVOURITE));
                    getActivity().invalidateOptionsMenu();

                    /*
                    if(mFavourite == 1){
                        mFavouriteButton.setText("Unmark favourite");
                    }
                    else{
                        mFavouriteButton.setText("Mark favourite");

                    }
                    */
                    break;
                }
                case MOVIE_EXTRA_LOADER:{
                    //TODO fix refresh data
                    mExtraCursorAdapter.swapCursor(cursor);
                    //mExtraCursorAdapter.notifyDataSetChanged();
                    break;
                }
                default:break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(TAG,"onLoaderReset");

    }
}



package com.skynet.dwhsu.cinema_browser.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.sync.MovieSyncAdapter;

/**
 *   Main activity of the app, contains the grid view fragment in single pane view or master detail
 *   fragments in two pane view

 */

public class MovieBrowserGridActivity extends AppCompatActivity {
    private static String TAG = "MovieBrowserGridActivity";
    private boolean mTwoPane;
    private Fragment mGridFrag;
    private Fragment mDetailFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_browser_activity);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState == null) {

            // set two pane set up
            if (findViewById(R.id.movie_detail_container) != null) {
                Log.d("Browser","set two pane");
                mTwoPane = true;
                if (savedInstanceState == null) {
                    mDetailFrag = new MovieDetailFragment();
                    Bundle args = new Bundle();
                    args.putBoolean("hasData",false);
                    args.putBoolean("twoPane",true);
                    mDetailFrag.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.movie_detail_container, mDetailFrag)
                            .commit();
                }
            } else {
                mTwoPane = false;
            }

            mGridFrag = new MovieRecyclerGridFragment();
            Bundle args = new Bundle();
            args.putBoolean("twoPane",mTwoPane);
            mGridFrag.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movieGridFragment, mGridFrag)
                    .commit();
        }
        else{
            //Log.d(TAG,"restore mGridFrag");
            //mGridFrag = getSupportFragmentManager().getFragment(savedInstanceState, "mGridFrag");
            /*
            mTwoPane = savedInstanceState.getBoolean("mTwoPane");
            mGridFrag = getSupportFragmentManager().getFragment(savedInstanceState, "mGridFrag");
            if(mTwoPane == true){
                //TODO may have to change this
                Bundle args = new Bundle();
                args.putBoolean("hasData",false);
                mDetailFrag.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, mDetailFrag)
                        .commit();

            }
            else {

            }
            Bundle args = new Bundle();
            args.putBoolean("twoPane",mTwoPane);
            mGridFrag.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movieGridFragment, mGridFrag)
                    .commit();
                    */
        }
        // TODO sync disabled for test
        MovieSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onResume(){
        Log.d(TAG,"[onResume]");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_grid, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onPause() {
        Log.d(TAG,"[onPause]");
        super.onPause();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"[onSaveInstanceState]");
        super.onSaveInstanceState(outState);
        outState.putBoolean("mTwoPane",mTwoPane);
        //Save the fragment's instance
        //getSupportFragmentManager().putFragment(outState, "mGridFrag", mGridFrag);
    }
    public boolean getOrientation(){
        return mTwoPane;
    }
}
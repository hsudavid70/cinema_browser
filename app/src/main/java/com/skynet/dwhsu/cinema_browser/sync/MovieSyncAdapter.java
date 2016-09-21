package com.skynet.dwhsu.cinema_browser.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import com.skynet.dwhsu.cinema_browser.MovieApp;
import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.skynet.dwhsu.cinema_browser.svc.*;

/**
 *   Syncadapter
 */

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    // automatic sync interval is 1.5 days since tmdb is refreshed once every day
    //public static final int SYNC_INTERVAL = 60*60*36;
    public static final int SYNC_INTERVAL = 10;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static Context mContext;


    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        //TODO sync adapter will be implemented
        // On sync,  fetch latest list of top rated and popular movies and delete old data
        reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+MovieApp.POPULAR_URL+ MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieEntry.CONTENT_URI,mContext),MovieApp.mimeType.BINARY);
        reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+MovieApp.TOP_URL+MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieEntry.CONTENT_URI,mContext),MovieApp.mimeType.BINARY);
        return;
    }


    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }


    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if ( null == accountManager.getPassword(newAccount) ) {


            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }


            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        MovieSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
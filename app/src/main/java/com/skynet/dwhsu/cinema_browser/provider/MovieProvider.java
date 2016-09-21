package com.skynet.dwhsu.cinema_browser.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.skynet.dwhsu.cinema_browser.BuildConfig;
import com.skynet.dwhsu.cinema_browser.svc.RESTIntentService;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract.MovieEntry;


public class MovieProvider extends ContentProvider {
    private static final String TAG = "MovieProvider";
    private volatile MovieDBHelper mDBHelper;

    public static final int MOVIE_LIST= 100;
    public static final int MOVIE_DETAIL_LIST= 103;

    public static final int MOVIE_DETAIL_BY_ID = 105;
    public static final int MOVIE_EXTRA_BY_ID = 106;
    public static final int MOVIE_REVIEW_BY_ID = 107;
    public static final int MOVIE_TRAILER_BY_ID = 109;
    public static final int MOVIE_EXTRA_ALL = 110;

    private static final String[] projectionAll = {"*"};
    private static final String[] projectionAllExtra = {"*"};

    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);

    }

    private static final SQLiteQueryBuilder sMovieExtraQueryBuilder;
    static {
        sMovieExtraQueryBuilder = new SQLiteQueryBuilder();
        sMovieExtraQueryBuilder.setTables(MovieContract.MovieExtraEntry.TABLE_NAME);

    }


    //detail.movie_id = ?
    private static final String sMovieDetailSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COL_MOVIE_ID+ " = ? ";


    private static final String sMovieExtraSelection =
            MovieContract.MovieExtraEntry.TABLE_NAME+
                    "." + MovieContract.MovieExtraEntry.COL_MOVIE_ID+ " = ? ";

    private Cursor getMovieExtras(Uri uri,String sortOrder) {
        String movie_id = MovieContract.getPathSegOne(uri);

        String[] selectionArgs;

        selectionArgs = new String[]{movie_id};
        return sMovieExtraQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projectionAllExtra,
                sMovieExtraSelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovies(String sel, String[] selArgs,String sortOrder, String limit) {

        return sMovieQueryBuilder.query(mDBHelper.getReadableDatabase(),
                projectionAll,
                sel,
                selArgs,
                null,
                null,
                sortOrder,
                limit
        );
    }



    private Cursor getMovieDetail(Uri uri, String[] proj, String sortOrder) {
        String movie_id = MovieContract.getPathSegOne(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{movie_id};
        selection = sMovieDetailSelection;

        return sMovieQueryBuilder.query(mDBHelper.getReadableDatabase(),
                proj,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE,
                MOVIE_LIST);

        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE+"/*",
                MOVIE_DETAIL_BY_ID);
        // TODO check matching issues
        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE_EXTRA+"/"+MovieContract.PATH_MOVIE_REVIEW+"/*",
                MOVIE_REVIEW_BY_ID);
        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE_EXTRA+"/"+MovieContract.PATH_MOVIE_TRAILER+"/*",
                MOVIE_TRAILER_BY_ID);
        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE_EXTRA,
                MOVIE_EXTRA_ALL);
        uriMatcher.addURI(
                MovieContract.AUTHORITY,
                MovieContract.PATH_MOVIE_EXTRA+"/*",
                MOVIE_EXTRA_BY_ID);
    }




    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate");
        mDBHelper = new MovieDBHelper(getContext());
        return null != mDBHelper;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MOVIE_LIST:
                return MovieContract.MovieEntry.CONTENT_TYPE;

            case MOVIE_DETAIL_BY_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_EXTRA_BY_ID:
                return MovieContract.MovieExtraEntry.CONTENT_ITEM_TYPE;
            case MOVIE_REVIEW_BY_ID:
                return MovieContract.MovieExtraEntry.REVIEW_CONTENT_TYPE;
            case MOVIE_TRAILER_BY_ID:
                return MovieContract.MovieExtraEntry.TRAILER_CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: "+uri);
        }
    }

    @Override
    public String[] getStreamTypes(Uri uri, String mimeTypeFilter){
        switch(uriMatcher.match(uri)){
            default:
                return null;
        }
    }


    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "insert@" + uri); }

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case MOVIE_LIST: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, vals);
                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){

        Log.d(TAG,"[bulkInsert] "+uri.toString());

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIE_LIST:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {

                        SQLiteStatement pstmt = db.compileStatement("INSERT OR REPLACE INTO "+ MovieEntry.TABLE_NAME +" ("+ MovieEntry.COL_MOVIE_ID + "," +
                                MovieEntry.COL_POSTER_PATH + "," +
                                MovieEntry.COL_POPULARITY + "," +
                                MovieEntry.COL_RATING + "," +
                                MovieEntry.COL_ORIG_TITLE + "," +
                                MovieEntry.COL_OVERVIEW + "," +
                                MovieEntry.COL_RELEASE_DATE + "," +
                                MovieEntry.COL_FAVOURITE + ")" + "VALUES(" +
                                "?,?,?,?,?,?,?, (SELECT "+MovieEntry.COL_FAVOURITE+" FROM "+MovieEntry.TABLE_NAME + " WHERE " + MovieEntry.COL_FAVOURITE + "=1 AND "+MovieEntry.COL_MOVIE_ID +" =?)"
                                +")");
                        int movie_id = value.getAsInteger(MovieEntry.COL_MOVIE_ID);
                        pstmt.bindLong(1,movie_id);
                        pstmt.bindString(2,value.getAsString(MovieEntry.COL_POSTER_PATH));
                        pstmt.bindDouble(3,value.getAsDouble(MovieEntry.COL_POPULARITY));
                        pstmt.bindDouble(4,value.getAsDouble(MovieEntry.COL_RATING));
                        pstmt.bindString(5,value.getAsString(MovieEntry.COL_ORIG_TITLE));
                        pstmt.bindString(6,value.getAsString(MovieEntry.COL_OVERVIEW));
                        pstmt.bindString(7,value.getAsString(MovieEntry.COL_RELEASE_DATE));
                        pstmt.bindLong(8,movie_id);
                        long _id = pstmt.executeInsert();



                        //long _id = db.insertWithOnConflict(MovieContract.MovieEntry.TABLE_NAME, null, value,SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    //ContentValues sync_stat = new ContentValues();
                    //sync_stat.put("SYNC_TYPE","MOVIE");
                    //sync_stat.put("SYNC_TIME",System.currentTimeMillis());
                    //db.insert("SYNC_LOG",null,sync_stat);
                    db.setTransactionSuccessful();
                }catch(SQLiteConstraintException e) {
                    Log.d(TAG,"constraint violation");
                }finally {
                    db.endTransaction();
                }
                break;

            case MOVIE_EXTRA_BY_ID:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieExtraEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                catch(SQLiteConstraintException e) {
                    Log.d(TAG,"constraint violation");
                }finally {
                    db.endTransaction();
                }
                break;

            default:
                return super.bulkInsert(uri, values);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;

    }

    @Override
    public int update(Uri uri, ContentValues vals, String sel, String[] selArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "update@" + uri); }

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {

            case MOVIE_DETAIL_BY_ID:
                String movie_id = MovieContract.getPathSegOne(uri);
                rowsUpdated = db.update(
                        MovieContract.MovieEntry.TABLE_NAME, vals, sMovieDetailSelection, new String[] {movie_id} );
                //TODO what if movie_id changed : highly unlikely
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String sel, String[] selArgs) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "delete@" + uri); }

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == sel ) sel = "1";
        switch (match) {
            case MOVIE_LIST:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, null, null);
                break;

            case MOVIE_EXTRA_ALL:
                rowsDeleted = db.delete(
                        MovieContract.MovieExtraEntry.TABLE_NAME, sel, selArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    //@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    //@SuppressWarnings("fallthrough")
    @Override
    // Loader calls Cursor query ?
    public Cursor query(
            Uri uri,
            String[] proj,
            String sel,
            String[] selArgs,
            String ord)
    {

       if (BuildConfig.DEBUG) { Log.d(TAG, "query@" + uri); }

       Cursor retCursor;
        Bundle req_param = new Bundle();
       switch(uriMatcher.match(uri)){
           case MOVIE_LIST:{
               //retCursor = getMovies(sel,selArgs,ord,"20");
               retCursor = getMovies(sel,selArgs,ord,null);
               // TODO empty favourite view will not generate new call
               if(retCursor.getCount()==0 && sel == null){
                   Log.d(TAG,"cached data not found");
                   req_param.putString("REQ_TYPE", "POPULAR");
                   movieREST(req_param);
                   req_param.putString("REQ_TYPE", "TOP_RATED");
                   movieREST(req_param);
               }
               break;
           }

           case MOVIE_DETAIL_BY_ID:{
               retCursor = getMovieDetail(uri,proj,ord);

               break;
           }
           case MOVIE_EXTRA_BY_ID:{
               retCursor = getMovieExtras(uri,ord);
               if(retCursor.getCount()==0){
                   String movie_id = MovieContract.getPathSegOne(uri);
                   // prefetch extras
                   req_param.putString("REQ_TYPE", "REVIEW");
                   req_param.putString("movie_id", movie_id);
                   movieREST(req_param);
                   //req_param.clear();
                   req_param.putString("REQ_TYPE", "TRAILER");
                   //req_param.putString("movie_id", movie_id);
                   movieREST(req_param);
               }
               break;
           }

           default:
               throw new UnsupportedOperationException("Unknown uri: "+uri);
       }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
       return retCursor;
    }

    private void movieREST(Bundle bundle) {
        String rest_token = "";

        rest_token = RESTIntentService.movieRequest(getContext(), bundle);
    }
}

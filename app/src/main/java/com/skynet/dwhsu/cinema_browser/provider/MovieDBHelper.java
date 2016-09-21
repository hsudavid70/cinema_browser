package com.skynet.dwhsu.cinema_browser.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.skynet.dwhsu.cinema_browser.provider.MovieContract.MovieExtraEntry;

import static com.skynet.dwhsu.cinema_browser.provider.MovieContract.*;

public class MovieDBHelper extends SQLiteOpenHelper {

    public static final String TAG = "MovieDBHelper";

    private static final int VERSION = 5;

    static final String DB_FILE = "movies.db";

    public MovieDBHelper(Context context) {
        super(context, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Setup table onCreate
        final String CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COL_MOVIE_ID + " INTEGER NOT NULL, "+
                MovieEntry.COL_RATING + " REAL NOT NULL, " +
                MovieEntry.COL_POPULARITY + " REAL NOT NULL, " +
                MovieEntry.COL_ORIG_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COL_OVERVIEW + " TEXT NOT NULL, " +
                MovieEntry.COL_RELEASE_DATE + " TEXT NOT NULL, " +
                MovieEntry.COL_POSTER_PATH + " TEXT NOT NULL, " +
                MovieEntry.COL_FAVOURITE + " INTEGER DEFAULT 0," +
                MovieEntry.COL_CREATION + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                " UNIQUE(" + MovieExtraEntry.COL_MOVIE_ID + ") ON CONFLICT REPLACE"+
                " );";



        final String CREATE_MOVIE_EXTRA_TABLE = "CREATE TABLE " + MovieExtraEntry.TABLE_NAME + " (" +
                MovieExtraEntry._ID + "  TEXT PRIMARY KEY NOT NULL," +
                MovieExtraEntry.COL_MOVIE_ID+ " INTEGER NOT NULL, " +
                MovieExtraEntry.COL_REVIEW_LINK+ " TEXT , " +
                MovieExtraEntry.COL_REVIEW_AUTHOR+ " TEXT , " +
                MovieExtraEntry.COL_TRAILER_LINK + " TEXT , " +
                MovieExtraEntry.COL_TRAILER_NAME + " TEXT , " +
                MovieExtraEntry.COL_CREATION + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                " UNIQUE(" + MovieExtraEntry._ID + ") ON CONFLICT REPLACE,"+
                //" FOREIGN KEY (" + MovieExtraEntry.COL_MOVIE_ID + ") REFERENCES " +
                //PopularEntry.TABLE_NAME + " (" + PopularEntry.COL_MOVIE_ID + ")," +
                " FOREIGN KEY (" + MovieExtraEntry.COL_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COL_MOVIE_ID + ") ON DELETE CASCADE" +
                " );";

        /* final String CREATE_LOG_TABLE = "CREATE TABLE " + "SYNC_LOG" + " (" +
                MovieExtraEntry._ID + "  INTEGER PRIMARY KEY AUTOINCREMENT," +
                "SYNC_TYPE" + " TEXT , " +
                "SYNC_TIME"+ " INTEGER NOT NULL " +
                " );";*/

        db.execSQL(CREATE_MOVIES_TABLE);
        db.execSQL(CREATE_MOVIE_EXTRA_TABLE);
        //db.execSQL(CREATE_LOG_TABLE);

    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        //TODO WLA disabled for TESTING
        if(db.enableWriteAheadLogging()){
         Log.d("onConfigure","WriteAheadLogging enabled");
        }
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Delete tables on upgrade
        try {
            db.execSQL("drop table if exists " + MovieEntry.TABLE_NAME);
            db.execSQL("drop table if exists " + MovieExtraEntry.TABLE_NAME);
            //db.execSQL("drop table if exists " + "SYNC_LOG");
        }
        catch (SQLiteException e) {
            Log.e(TAG,"Handle upgrade exception");
        }

        onCreate(db);
    }
}

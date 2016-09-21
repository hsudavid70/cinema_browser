package com.skynet.dwhsu.cinema_browser.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MovieContract {
    private MovieContract() {
    }

    // content provider base name and uri
    public static final String AUTHORITY = "com.skynet.dwhsu.cinema_browser";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // content provider paths
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_MOVIE_TRAILER = "trailer";
    public static final String PATH_MOVIE_REVIEW = "review";
    public static final String PATH_MOVIE_EXTRA = "extra";

    // schema two ordering of movies with foreign key set to details


    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movies";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_POSTER_PATH = "poster_path";
        public static final String COL_RATING = "vote_average";
        public static final String COL_ORIG_TITLE = "original_title";
        public static final String COL_OVERVIEW = "overview";
        public static final String COL_RELEASE_DATE = "release_date";
        public static final String COL_POPULARITY = "popularity";
        public static final String COL_FAVOURITE = "favourite";
        public static final String COL_CREATION = "creation";

        //uri for item with column id
        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        public static Uri buildMovieDetailUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }
    }

    // Extra information about movie (traler and reviews)
    public static final class MovieExtraEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_MOVIE_EXTRA).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE_EXTRA;
        public static final String TRAILER_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE_EXTRA + "/" + PATH_MOVIE_TRAILER;
        public static final String REVIEW_CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE_EXTRA + "/" + PATH_MOVIE_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE_EXTRA;

        public static final String TABLE_NAME = "movie_extras";
        public static final String COL_MOVIE_ID = "movie_id";
        public static final String COL_REVIEW_LINK = "review_link";
        public static final String COL_TRAILER_LINK = "trailer_link";
        public static final String COL_REVIEW_AUTHOR = "review_author";
        public static final String COL_TRAILER_NAME = "trailer_name";
        public static final String COL_CREATION = "creation";

        public static Uri buildMovieExtraUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(movie_id).build();
        }

        public static Uri buildMovieReviewUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_REVIEW).appendPath(movie_id).build();
        }

        public static Uri buildMovieTrailerUri(String movie_id) {
            return CONTENT_URI.buildUpon().appendPath(PATH_MOVIE_TRAILER).appendPath(movie_id).build();
        }
    }

    /*
        Helper functions to generate URI for queries

     */
    public static String getPathSegOne(Uri uri) {
        return uri.getPathSegments().get(1).toString();
    }

    public static String getPathSegTwo(Uri uri) {
        return uri.getPathSegments().get(2).toString();
    }
}
package com.skynet.dwhsu.cinema_browser;

import android.app.Application;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by davidhsu on 5/31/16.
 * App wide definitions and settings
 */
public class MovieApp extends Application{
    public static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String API_BASE_URL = "http://api.themoviedb.org/3/";
    //TODO API KEY is appended to ?api_key=
    public static final String API_KEY_URL = "?api_key=f7407d6687acc5a8399f44083df42604";
    public static final String POPULAR_URL = "movie/popular";
    public static final String TOP_URL = "movie/top_rated";
    public static final String DEFAULT_POSTER_URL = "http://image.tmdb.org/t/p/w185/";


    public enum HttpMethod { GET, PUT, POST, DELETE; }
    public enum mimeType {
        BINARY, JSON, XML
    }

    // REST operations
    public enum Op {
        NOOP, POPULAR, TOP_RATED, REVIEW, TRAILER;

        public static Op toOp(int code) {
            Op[] ops = Op.values();
            code = (code * -1) - 1;
            return ((0 > code) || (ops.length <= code))
                    ? NOOP
                    : ops[code];
        }
        public int toInt() { return (ordinal() + 1) * -1; }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        // Picasso settings
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(false);
        Picasso.setSingletonInstance(built);
    }
}

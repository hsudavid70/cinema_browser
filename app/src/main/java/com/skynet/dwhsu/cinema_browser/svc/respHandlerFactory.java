package com.skynet.dwhsu.cinema_browser.svc;

import android.content.Context;
import android.net.Uri;

/**
 * Creates instances of response handler objects
 */
public class respHandlerFactory {
    private static final String TAG = "respHandlerFactory";
    private volatile static respHandlerFactory instance;

    /** Returns singleton class instance */
    public static respHandlerFactory getInstance() {
        if (instance == null) {
            synchronized (respHandlerFactory.class) {
                if (instance == null) {
                    instance = new respHandlerFactory();
                }
            }
        }
        return instance;
    }

    protected respHandlerFactory(){

    }

    public movieJsonHandler newMovieJsonHandler(Uri uri,Context context){
        return new movieJsonHandler(uri,context);

    }

}

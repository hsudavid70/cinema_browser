package com.skynet.dwhsu.cinema_browser.svc;

import com.skynet.dwhsu.cinema_browser.MovieApp;

/**
 * Creates instances of http request objects
 */
public class reqHandlerFactory {
    private static final String TAG = "respHandlerFactory";
    private volatile static reqHandlerFactory instance;

    /** Returns singleton class instance */
    public static reqHandlerFactory getInstance() {
        if (instance == null) {
            synchronized (reqHandlerFactory.class) {
                if (instance == null) {
                    instance = new reqHandlerFactory();
                }
            }
        }
        return instance;
    }

    protected reqHandlerFactory(){

    }

    public httpReqHandler newHttpRequest(MovieApp.HttpMethod method,
                                         String url,
                                         String payload,
                                         RESTIntentService.ResponseHandler hdlr,
                                         MovieApp.mimeType type){
        return new httpReqHandler(method,url,payload,hdlr,type);

    }

}

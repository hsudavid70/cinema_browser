package com.skynet.dwhsu.cinema_browser.svc;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.skynet.dwhsu.cinema_browser.MovieApp;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
     IntentService for REST API
 */

public class RESTIntentService extends IntentService {

    private static final String TAG = "REST";

    public static final String XACT = "RESTIntentService.XACT";
    public static final String ID = "RESTIntentService.ID";



    public RESTIntentService() {
        super(TAG);
        // IMPORTANT: redeliver on restore
        setIntentRedelivery(false);
    }
    public static String movieRequest(Context ctxt, Bundle vals) {
        Log.d(TAG,"handling request "+vals.getString("REQ_TYPE"));

        Intent intent = new Intent(ctxt, RESTIntentService.class);

        MovieApp.Op request = MovieApp.Op.NOOP;
        switch(vals.getString("REQ_TYPE")){
            case "POPULAR": request = MovieApp.Op.POPULAR; break;
            case "TOP_RATED": request = MovieApp.Op.TOP_RATED; break;
            case "REVIEW": request = MovieApp.Op.REVIEW;
                           intent.putExtra("movie_id",vals.getString("movie_id"));
                           break;
            case "TRAILER": request = MovieApp.Op.TRAILER;
                           intent.putExtra("movie_id",vals.getString("movie_id"));
                           break;
            default: break;
        }
        String xact = UUID.randomUUID().toString();

        intent.putExtra("RestOP", request.toInt());
        intent.putExtra(RESTIntentService.XACT, xact);
        ctxt.startService(intent);

        return intent.getStringExtra(RESTIntentService.XACT);
    }




    public interface ResponseHandler {
        void handleResponse(InputStream in) throws IOException;

    }



    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG,"onHandleIntent");
        Bundle args = intent.getExtras();

           int op = 0;
           if (args.containsKey("RestOP")) { op = args.getInt("RestOP"); }
           switch (MovieApp.Op.toOp(op)) {
               case POPULAR:
                   Log.d("Intent sendHttpRequest","POPULAR");
                   reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+MovieApp.POPULAR_URL+ MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieEntry.CONTENT_URI,getApplicationContext()),MovieApp.mimeType.BINARY);
                   break;
               case TOP_RATED:
                   Log.d("Intent sendHttpRequest","TOP_RATED");
                   reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+MovieApp.TOP_URL+MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieEntry.CONTENT_URI,getApplicationContext()),
                           MovieApp.mimeType.BINARY);
                   break;
               case REVIEW:
                   Log.d("Intent sendHttpRequest","REVIEW");
                   reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+"movie/"+args.getString("movie_id")+"/reviews"+MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieExtraEntry.buildMovieReviewUri(args.getString("movie_id")),getApplicationContext()),
                           MovieApp.mimeType.BINARY);
                   break;
               case TRAILER:
                   Log.d("Intent sendHttpRequest","TRAILER");
                   reqHandlerFactory.getInstance().newHttpRequest(MovieApp.HttpMethod.GET,MovieApp.API_BASE_URL+"movie/"+args.getString("movie_id")+"/videos"+MovieApp.API_KEY_URL,null,respHandlerFactory.getInstance().newMovieJsonHandler(MovieContract.MovieExtraEntry.buildMovieTrailerUri(args.getString("movie_id")),getApplicationContext()),
                           MovieApp.mimeType.BINARY);
                   break;
               default:
                   //cleanup(args, null);
                   throw new IllegalArgumentException("Unrecognized op: " + op);
           }

    }
}
package com.skynet.dwhsu.cinema_browser.svc;

import android.util.Log;

import com.skynet.dwhsu.cinema_browser.BuildConfig;
import com.skynet.dwhsu.cinema_browser.MovieApp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *   http request object
 */


public class httpReqHandler {
    public static String TAG = "httpReqHandler";
    public static final String HEADER_ENCODING = "Accept-Encoding";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String MIME_ANY = "*/*";
    public static final String MIME_JSON = "application/json;charset=UTF-8";
    public static final String MIME_XML = "application/xml;charset=UTF-8";
    public static final String MIME_OCTET= "application/octet-stream";
    public static final String ENCODING_NONE = "identity";
    public static final String ENCODING_GZIP = "gzip";

    public static final int HTTP_READ_TIMEOUT = 5 * 1000; // ms
    public static final int HTTP_CONN_TIMEOUT = 5 * 1000; // ms

    public httpReqHandler(MovieApp.HttpMethod method,
                          String url,
                          String payload,
                          RESTIntentService.ResponseHandler hdlr,
                          MovieApp.mimeType type){
        Log.d(TAG,"sending http request");
        try {
            sendHttpRequest(method, url, payload, hdlr, type);
        }
        catch(IOException e){

        }
    }

    private int sendHttpRequest(
            MovieApp.HttpMethod method,
            String url,
            String payload,
            RESTIntentService.ResponseHandler hdlr,
            MovieApp.mimeType type)
            throws IOException
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sending " + method + " @" + url);
        }

        HttpURLConnection conn
                = (HttpURLConnection) new URL(url).openConnection();
        int code = HttpURLConnection.HTTP_UNAVAILABLE;
        try {
            conn.setReadTimeout(HTTP_READ_TIMEOUT);
            conn.setConnectTimeout(HTTP_CONN_TIMEOUT);
            conn.setRequestMethod(method.toString());
            //conn.setRequestProperty(HEADER_USER_AGENT, USER_AGENT);
            conn.setRequestProperty(HEADER_ENCODING, ENCODING_NONE);
            conn.setRequestProperty(HEADER_ACCEPT, MIME_ANY);

            if (null != hdlr) {

                conn.setDoInput(true);
            }

            if (null != payload) {

                conn.setFixedLengthStreamingMode(payload.length());
                conn.setDoOutput(true);

                conn.connect();
                Writer out = new OutputStreamWriter(
                        new BufferedOutputStream(conn.getOutputStream()),
                        "UTF-8");
                out.write(payload);
                out.flush();
            }

            code = conn.getResponseCode();

            if (null != hdlr) {


                hdlr.handleResponse(new BufferedInputStream(conn.getInputStream()));
            }
        }
        finally {
            if (null != conn) {
                try { conn.disconnect(); } catch (Exception e) { }
            }
        }

        return code;
    }
}


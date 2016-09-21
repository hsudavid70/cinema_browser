package com.skynet.dwhsu.cinema_browser.svc;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.skynet.dwhsu.cinema_browser.provider.MovieProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 *   Response processing object

 */

public class movieJsonHandler implements RESTIntentService.ResponseHandler {
    private static final String TAG = "movieJsonHandler";
    private Uri mUri;
    private Context mContext;
    public movieJsonHandler(Uri uri, Context context){
        mContext = context;
        mUri = uri;
    }

    @Override
    public void handleResponse(InputStream in) throws IOException {
        String json_str = getStringFromInputStream(in);
        //Log.d(TAG, json_str);

        try {
            switch(MovieProvider.uriMatcher.match(mUri)) {
                case MovieProvider.MOVIE_LIST:
                    movieListJsonParse(json_str);
                    break;
                case MovieProvider.MOVIE_REVIEW_BY_ID:
                    movieReviewJsonParse(json_str);
                    break;
                case MovieProvider.MOVIE_TRAILER_BY_ID:
                    movieTrailerJsonParse(json_str);
                    break;
                default:
                    Log.d(TAG," no uri match: "+mUri.toString());
                    break;
            }
        }
        catch(JSONException e) {
            Log.e("movieJsonHandler","json parse error");
        }
    }

    public void movieListJsonParse(String in) throws JSONException {
        try {
            JSONObject json_obj = new JSONObject(in);
            String total_results = json_obj.getString("total_results");
            String total_pages = json_obj.getString("total_pages");
            String current_page = json_obj.getString("page");
            JSONArray page_results_array = json_obj.getJSONArray("results");

            Vector<ContentValues> movieV = new Vector<>(page_results_array.length());
           // Vector<ContentValues> detailV = new Vector<>(page_results_array.length());

            for( int i = 0; i < page_results_array.length(); i++){
                //Log.d(TAG,page_results_array.getJSONObject(i).getString("title"));

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieContract.MovieEntry.COL_MOVIE_ID,page_results_array.getJSONObject(i).getString("id"));
                movieValues.put(MovieContract.MovieEntry.COL_POSTER_PATH,page_results_array.getJSONObject(i).getString("poster_path"));
                movieValues.put(MovieContract.MovieEntry.COL_RATING,page_results_array.getJSONObject(i).getString("vote_average"));
                movieValues.put(MovieContract.MovieEntry.COL_POPULARITY,page_results_array.getJSONObject(i).getString("popularity"));
                movieValues.put(MovieContract.MovieEntry.COL_ORIG_TITLE,page_results_array.getJSONObject(i).getString("original_title"));
                movieValues.put(MovieContract.MovieEntry.COL_OVERVIEW,page_results_array.getJSONObject(i).getString("overview"));
                movieValues.put(MovieContract.MovieEntry.COL_RATING,page_results_array.getJSONObject(i).getDouble("vote_average"));
                movieValues.put(MovieContract.MovieEntry.COL_RELEASE_DATE,page_results_array.getJSONObject(i).getString("release_date"));

                movieV.add(movieValues);

            }
            if(movieV.size() > 0){
                ContentValues[] movieArray = new ContentValues[movieV.size()];
                movieV.toArray(movieArray);
                mContext.getContentResolver().bulkInsert(mUri,movieArray);
            }


        }catch(JSONException e){
            Log.d(TAG,"JSON parse error");

        }

    }

    public void movieReviewJsonParse(String in) throws JSONException {
        Log.d(TAG,"movieReviewJsonParse "+mUri.toString());
        try {
            JSONObject json_obj = new JSONObject(in);
            JSONArray page_results_array = json_obj.getJSONArray("results");
            Vector<ContentValues> reviewV = new Vector<>(page_results_array.length());
            String movie_id = mUri.getPathSegments().get(2).toString();
            for( int i = 0; i < page_results_array.length(); i++) {
                ContentValues reviewValues = new ContentValues();
                reviewValues.put(MovieContract.MovieExtraEntry._ID,page_results_array.getJSONObject(i).getString("id"));
                reviewValues.put(MovieContract.MovieExtraEntry.COL_MOVIE_ID,Integer.parseInt(movie_id));
                reviewValues.put(MovieContract.MovieExtraEntry.COL_REVIEW_AUTHOR,page_results_array.getJSONObject(i).getString("author"));
                reviewValues.put(MovieContract.MovieExtraEntry.COL_REVIEW_LINK,page_results_array.getJSONObject(i).getString("url"));
                reviewV.add(reviewValues);

            }
            if(reviewV.size()>0){
                ContentValues[] reviewArray = new ContentValues[reviewV.size()];
                reviewV.toArray(reviewArray);
                //MovieContract.MovieExtraEntry.

                mContext.getContentResolver().bulkInsert( MovieContract.MovieExtraEntry.buildMovieExtraUri(MovieContract.getPathSegTwo(mUri)),reviewArray);
            }else{
                Log.d(TAG,"no reviews for this movie");
            }

        }catch(JSONException e){
            Log.d(TAG,"JSON parse error");

        }
    }

    public void movieTrailerJsonParse(String in) throws JSONException {
        Log.d(TAG,"movieTrailerJsonParse "+mUri.toString());
        try {
            JSONObject json_obj = new JSONObject(in);
            JSONArray page_results_array = json_obj.getJSONArray("results");
            Vector<ContentValues> trailerV = new Vector<>(page_results_array.length());
            String movie_id = mUri.getPathSegments().get(2).toString();
            for( int i = 0; i < page_results_array.length(); i++) {
                ContentValues trailerValues = new ContentValues();
                trailerValues.put(MovieContract.MovieExtraEntry._ID,page_results_array.getJSONObject(i).getString("id"));
                trailerValues.put(MovieContract.MovieExtraEntry.COL_MOVIE_ID,Integer.parseInt(movie_id));
                trailerValues.put(MovieContract.MovieExtraEntry.COL_TRAILER_NAME,page_results_array.getJSONObject(i).getString("name"));
                //TODO parse more link info
                trailerValues.put(MovieContract.MovieExtraEntry.COL_TRAILER_LINK,page_results_array.getJSONObject(i).getString("key"));
                trailerV.add(trailerValues);
            }
            if(trailerV.size()>0){
                ContentValues[] trailerArray = new ContentValues[trailerV.size()];
                trailerV.toArray(trailerArray);
                mContext.getContentResolver().bulkInsert( MovieContract.MovieExtraEntry.buildMovieExtraUri(MovieContract.getPathSegTwo(mUri)),trailerArray);
            }else{
                Log.d(TAG,"no trailers for this movie");

            }
        }catch(JSONException e){
            Log.d(TAG,"JSON parse error");

        }
    }

    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
}
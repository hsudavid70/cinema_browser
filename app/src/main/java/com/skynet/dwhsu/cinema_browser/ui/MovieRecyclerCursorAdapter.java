package com.skynet.dwhsu.cinema_browser.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.skynet.dwhsu.cinema_browser.MovieApp;
import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 *  Cursor adaptor for grid of movie posters
 *
 */


public class MovieRecyclerCursorAdapter extends CursorRecyclerViewAdapter<MovieRecyclerCursorAdapter.ViewHolder>{
    private static final String TAG = "RecyclerCursorAdapter";
    private static Context mContext;
    public MovieRecyclerCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mContext = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rect_image_simpleview,parent,false);
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rect_image_simpleview,parent,false);
        //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rect_image_simple,parent,false);

        ViewHolder vh = new ViewHolder(itemView);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
        viewHolder.setId((cursor.getInt(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_MOVIE_ID))));
        String imageLink = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieEntry.COL_POSTER_PATH));
        viewHolder.setLink(imageLink);

        if (imageLink != null) {
            Log.d("bindView", MovieApp.DEFAULT_POSTER_URL + imageLink);
            viewHolder.rowProgressBar.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load(MovieApp.DEFAULT_POSTER_URL+imageLink).error(R.drawable.default_empty).into(viewHolder.gridImage, new Callback() {
                @Override
                public void onSuccess() {
                    viewHolder.rowProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() { viewHolder.rowProgressBar.setVisibility(View.GONE);
                }
            });
            //Glide.with(context).load(MovieApp.DEFAULT_POSTER_URL+imageLink).placeholder(R.drawable.default_empty).error(R.drawable.empty_image).into((RectImageView)convertView);
            //setIndicatorsEnabled(false)
        }
    }



    @Override public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final RectImageView gridImage;
        public final View grid_layout;
        public final ProgressBar rowProgressBar;
        public ViewHolder(View itemView){
            super(itemView);
            gridImage = (RectImageView) itemView.findViewById(R.id.grid_image);
            grid_layout = itemView.findViewById(R.id.grid_image_layout);
            rowProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
        public void setId(int id){
            //gridImage.setId(id);
            grid_layout.setId(id);
        }



        public void setLink(String link){
            //gridImage.setTag(link);
            grid_layout.setTag(link);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG,"onClick called");
        }
    }
}
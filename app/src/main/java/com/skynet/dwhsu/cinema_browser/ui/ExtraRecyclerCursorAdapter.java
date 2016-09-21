package com.skynet.dwhsu.cinema_browser.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skynet.dwhsu.cinema_browser.R;
import com.skynet.dwhsu.cinema_browser.provider.MovieContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 *    Cursor adaptor for movie trailer and reviews
 *
 */

public class ExtraRecyclerCursorAdapter extends CursorRecyclerViewAdapter<ExtraRecyclerCursorAdapter.ViewHolder>{
    private static final String TAG = "ExtraCursorAdapter";
    private static Context mContext;
    public ExtraRecyclerCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_movie_extras,parent,false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
        if(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_NAME))!=null){
            Log.d(TAG,"bind trailer item");
            Log.d(TAG,cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_NAME)));
            viewHolder.rowTopText.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_NAME)));
            //viewHolder.rowBottomText.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_LINK)));
            final String link = (cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_LINK)));
            //viewHolder.setLink(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_TRAILER_LINK)));
            //viewHolder.setLink("trailer");
            //viewHolder.rowImage_overlay.setVisibility(View.VISIBLE);
            viewHolder.rowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.getTag().equals("IMAGE")){
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + link));
                        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                            mContext.startActivity(intent);
                        } else {
                            Toast.makeText(mContext, "Youtube app not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            viewHolder.rowImage_overlay.setVisibility(View.GONE);
            viewHolder.rowProgressBar.setVisibility(View.VISIBLE);
            Picasso.with(mContext).load("http://img.youtube.com/vi/"+link+"/sddefault.jpg").error(R.drawable.default_empty).into(viewHolder.rowImage, new Callback() {
                @Override
                public void onSuccess()
                {

                    viewHolder.rowImage_overlay.setVisibility(View.VISIBLE);
                    viewHolder.rowProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError() {
                    viewHolder.rowImage_overlay.setVisibility(View.GONE);
                    viewHolder.rowProgressBar.setVisibility(View.GONE);
                }
            });
            //Picasso.with(mContext).load("http://img.youtube.com/vi/"+viewHolder.rowBottomText.getText()+"/hqdefault.jpg").placeholder(R.drawable.default_empty).error(R.drawable.empty_image).into(viewHolder.rowImage);
            //viewHolder.setType(0);
        }
        else{
            Log.d(TAG,"bind review item");
            Log.d(TAG,cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_REVIEW_AUTHOR)));
            String format = mContext.getResources().getString(R.string.format_movie_review);
            viewHolder.rowTopText.setText(String.format(format,cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_REVIEW_AUTHOR))));
            //viewHolder.rowBottomText.setText(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_REVIEW_LINK)));
            final String link = cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_REVIEW_LINK));
            //viewHolder.setLink(cursor.getString(cursor.getColumnIndexOrThrow(MovieContract.MovieExtraEntry.COL_REVIEW_LINK)));
            //viewHolder.setLink("review");
            Drawable review_icon = ContextCompat.getDrawable(mContext,R.drawable.review_comment_glyph);
            viewHolder.rowImage.setImageDrawable(review_icon);
            viewHolder.rowImage_overlay.setVisibility(View.GONE);
            viewHolder.rowImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(v.getTag().equals("IMAGE")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                        mContext.startActivity(browserIntent);
                        if (browserIntent.resolveActivity(mContext.getPackageManager()) != null) {
                            mContext.startActivity(browserIntent);
                        } else {
                            Toast.makeText(mContext, "Browser app not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            //viewHolder.setType(1);
        }
    }

    @Override public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public final ImageView rowImage;
        public final ImageView rowImage_overlay;
        public final TextView rowTopText;
        public final ProgressBar rowProgressBar;
        public ViewHolder(View itemView){
            super(itemView);
            rowImage = (ImageView) itemView.findViewById(R.id.list_item_extra_image);
            rowImage_overlay = (ImageView) itemView.findViewById(R.id.list_item_play);
            rowImage.setTag("IMAGE");
            rowTopText = (TextView) itemView.findViewById(R.id.list_item_extra_textview_top);
            rowProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
        /*
        public void setLink(Object link){
            rowBottomText.setTag(link);
        }*/


        @Override
        public void onClick(View v) {
            Log.d(TAG,"onClick called");

        }
    }
}
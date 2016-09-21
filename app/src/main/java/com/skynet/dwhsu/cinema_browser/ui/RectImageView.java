package com.skynet.dwhsu.cinema_browser.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Wrapper class for imageview to adjust dimension.
 *
 */
public class RectImageView extends ImageView {

    private boolean mSquare = true;

    public RectImageView(Context context) {
        super(context);
    }

    public RectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RectImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    public void setSquare(boolean square) {
        if (square != mSquare) {
            mSquare = square;
            requestLayout();
        }
    }

    public RectImageView getImageView() {
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // This is needed to resize on API 16
        if (mSquare) {
            setMeasuredDimension(getMeasuredWidth(),7*getMeasuredWidth()/5);
        }
    }


}
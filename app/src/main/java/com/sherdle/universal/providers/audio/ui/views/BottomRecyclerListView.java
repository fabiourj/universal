package com.sherdle.universal.providers.audio.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Simple RecyclerView on which top padding could be assigned and enable touch on view under.
 */
public class BottomRecyclerListView extends RecyclerView {

    public BottomRecyclerListView(Context context) {
        super(context);
    }

    public BottomRecyclerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BottomRecyclerListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(ev);
        View child = getChildAt(0);
        if (child == null || ev.getY() < child.getY()) {
            handled = false;
        }
        return handled;
    }
}

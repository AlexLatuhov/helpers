package com.latuhov.helpers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Latuhov on 11/25/16.
 */
public class DrawableBackgroundSelector implements View.OnTouchListener {
    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                filterView(view);
                break;
            case MotionEvent.ACTION_CANCEL:
                clearFilter(view);
                break;
            case MotionEvent.ACTION_UP:
                clearFilter(view);
                break;
        }
        return view.onTouchEvent(event);
    }

    private void clearFilter(View view) {
        if (view instanceof ViewGroup)
            clearChildren((ViewGroup) view);

        if (view instanceof ImageView)
            ((ImageView) view).clearColorFilter();
        else
            view.getBackground().clearColorFilter();
        view.invalidate();
    }

    private void filterChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (canBeModified(child)) filterView(child);
        }
    }

    private void clearChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (canBeModified(child)) clearFilter(child);
        }
    }

    private void filterView(View view) {
        if (view == null) return;
        String SHADOW_COLOR = "#B7B2B0";

        if (view instanceof ViewGroup)
            filterChildren((ViewGroup) view);

        if (view instanceof ImageView)
            ((ImageView) view).setColorFilter(Color.parseColor(SHADOW_COLOR), PorterDuff.Mode.MULTIPLY);
        else if (view.getBackground() != null)
            view.getBackground().setColorFilter(Color.parseColor(SHADOW_COLOR), PorterDuff.Mode.MULTIPLY);
        view.invalidate();
    }

    private boolean canBeModified(View view) {
        return view.getBackground() != null || view instanceof ImageView;
    }

    public static DrawableBackgroundSelector create() {
        return new DrawableBackgroundSelector();
    }

    public static void create(View... views) {
        for (View view : views) {
            if (view == null) return;
            view.setOnTouchListener(create());
        }
    }
}

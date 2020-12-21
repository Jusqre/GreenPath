package com.example.tmapgreentest;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener listener;
    private GestureDetector gestureDetector;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }
    public RecyclerViewItemClickListener(Context context, final RecyclerView recyclerView, final OnItemClickListener listener){
        this.listener = listener;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) { return true; }

            @Override
            public void onLongPress(MotionEvent e){
                View v = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if(v != null && listener != null){
                    listener.onItemLongClick(v, recyclerView.getChildAdapterPosition(v));
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View view = rv.findChildViewUnder(e.getX(), e.getY());
        if(view != null && gestureDetector.onTouchEvent(e)) {
            listener.onItemClick(view, rv.getChildAdapterPosition(view));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}

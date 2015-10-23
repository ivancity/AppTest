package com.design.ivan.apptest.interfaces;

/**
 * This is in case that a list is empty and we need to communicate this to the
 * Activity implementing it
 * Created by ivanm on 10/23/15.
 */
public interface CallBackEmptyList {
    public void onEmptyList(boolean isEmpty);
}

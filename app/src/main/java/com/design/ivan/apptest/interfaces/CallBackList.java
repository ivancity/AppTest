package com.design.ivan.apptest.interfaces;

import android.net.Uri;

/**
 * Created by ivanm on 10/20/15.
 * Callback called when an item on a list is clicked and we let the Activity know this
 * action.
 */
public interface CallBackList {
    public void onItemSelected(Uri uri);
}

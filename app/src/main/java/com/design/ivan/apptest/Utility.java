package com.design.ivan.apptest;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.design.ivan.apptest.appsync.AppSyncAdapter;

/**
 * Created by ivanm on 11/20/15.
 */
public class Utility {

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return true if the network is available
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the location status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @AppSyncAdapter.CategoryStatus
    int getCategoryStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_category_status_key), AppSyncAdapter.CATEGORY_STATUS_UNKNOWN);
    }

    static public void resetCategoryStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putInt(c.getString(R.string.pref_category_status_key), AppSyncAdapter.CATEGORY_STATUS_UNKNOWN);
        spEditor.apply();
    }


}

package com.design.ivan.apptest.appsync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.design.ivan.apptest.Constants;
import com.design.ivan.apptest.R;
import com.design.ivan.apptest.appdata.AppDataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by ivanm on 10/22/15.
 */
public class AppSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = AppSyncAdapter.class.getSimpleName();
    public static final int DEBUG_TIME = 30; //seconds


    // Interval at which to sync with the server, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    //public static final int SYNC_INTERVAL = DEBUG_TIME;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public AppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "ONPREFORMSYNC Called.");
        //getting necessary URL from extra and pass it to the method
        String rawJsonResponse = getServerData(Constants.category_url);

        try {
            if(rawJsonResponse != null)
                getCategoryFromJson(rawJsonResponse);
            else
                Log.d(TAG, "Something went wrong receiving raw json response");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                AppDataContract.CONTENT_AUTHORITY, bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        /*
        will create a new account if no com.design.ivan.apptest.FLAVORNAME.sync account exists.
        If this is the case, onAccountCreated will be called.
         */
        String password = accountManager.getPassword(newAccount);
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                Log.d(TAG, "AccountManager not able to add New Account.");
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest.Builder request = (new SyncRequest.Builder()).
                    syncPeriodic(syncInterval, flexTime);

            request.setSyncAdapter(account, authority);
            request.setExtras(new Bundle());
            ContentResolver.requestSync(request.build());
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * If an account is created this method will set a periodic execution of the sync adapter
     * @param newAccount
     * @param context
     */
    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        AppSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled. Set the
         * as true for automatic sync in the specified newAccount
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


    protected String getServerData(String urlServer){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String rawJsonResponse;

        try {
            // Construct the URL for the category json
            URL url = new URL(urlServer);

            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setRequestMethod("GET");
            //urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            rawJsonResponse = buffer.toString();
            //Log.d(TAG, rawJsonResponse);

            return rawJsonResponse;


        } catch (IOException e) {
            Log.e("GetCategoryTask", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("GetCategoryTask", "Error closing stream", e);
                }
            }
        }
    }

    /**
     * This method does multplie things. It makes sure that we don't have repeated values in the
     * Vector collection of incoming values from server. Once it is sure that we have values in
     * the Vector that doesn't exist in the data base. It will attempt to insert new values to the
     * data base.
     * @param jsonCategoryStr
     * @throws JSONException
     */
    private void getCategoryFromJson(String jsonCategoryStr) throws JSONException{
        final String KEY_DATA = "data";
        final String KEY_ID = "id";
        final String KEY_NAME = "name";
        final String KEY_URLIMAGE = "image_url";

        try{
            String name, imageUrl;
            int id;
            boolean isDuplicated;
            ContentValues categoryValues = null;

            JSONObject categoryJson = new JSONObject(jsonCategoryStr);

            //getting array of categories from json response
            JSONArray categoryArray = categoryJson.getJSONObject(KEY_DATA).getJSONArray(KEY_DATA);

            //Vector used for bulkInsert in ContentProvider. It is optimized to work for array of values.
            Vector<ContentValues> cVVector = new Vector<ContentValues>(categoryArray.length());

            for(int i = 0; i < categoryArray.length(); i++) {
                JSONObject categoryObj = categoryArray.getJSONObject(i);
                id = categoryObj.getInt(KEY_ID);
                name = categoryObj.getString(KEY_NAME);
                imageUrl = "http:" + categoryObj.getString(KEY_URLIMAGE);
                //Log.d(TAG, imageUrl);
                //attempts to add category to ContentValues but it checkes first if id already
                //exists in the database. If it doesn't exist it proceeds to add it in the Vector.
                categoryValues = addCategory(String.valueOf(id), imageUrl, name);
                if (categoryValues != null) {
                    isDuplicated = hasDuplicateById(categoryValues.getAsString(AppDataContract.
                            CategoryEntry.COLUMN_CATEGORY_API_ID)
                            ,cVVector
                            ,AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID);

                    if(!isDuplicated) {
                        cVVector.add(categoryValues);
                    }
                }
            }

            int inserted = 0;

            //add to database. Use Content Resolver to request for a bulk Insert.
            if (cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver().bulkInsert(AppDataContract.CategoryEntry.CONTENT_URI, cvArray);
            }

            Log.d(TAG, "APPSYNCADPATER Complete. " + inserted + " Inserted");

        } catch(JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Checks if the new id coming from the server already exits in the Vector of incoming values
     * from server
     * @param id Id to find in the Vector of incoming values from the server
     * @param contentValueVector Collection of all values coming from server.
     * @param contentKey Depending if we are handling Product or Category we will have different keys.
     * @return
     */
    protected boolean hasDuplicateById(String id, Vector<ContentValues> contentValueVector
            , String contentKey){
        String idInVector;
        for (ContentValues contentValues : contentValueVector) {
            idInVector = contentValues.getAsString(contentKey);
            if(idInVector.contentEquals(id))
                return true;
        }
        return false;
    }

    /**
     * It will attempt to create a ContentValues object but first will check if the apiId exits already
     * in the database. If it already exists it will return a null. If it doesn't exist it will create
     * a new ContentValues and return it, in preparation for the bulkInsert later on.
     * @param apiId Id coming from the server and what we need to check if it already exists in the db
     * @param urlImg description url of the image that we need to download
     * @param categoryName name of the coategory in string
     * @return
     */
    private ContentValues addCategory(String apiId, String urlImg, String categoryName){
        String categoryId;

        ContentValues categoryValues = null;

        //Check if apiId exists already in the database

        Cursor categoryCursor = getContext().getContentResolver().query(
                AppDataContract.CategoryEntry.CONTENT_URI,
                new String[]{AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID},
                AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID + " = ?",
                new String[]{apiId},
                null);

        if (categoryCursor.moveToFirst()) {
            //category exists thus don't do anything and send null as response.
            //DEBUG CODE
            //int categoryIdIndex = categoryCursor.getColumnIndex(AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID);
            //categoryId = categoryCursor.getString(categoryIdIndex);
            //Log.d(TAG, "ApId from server already exists in the db: " + categoryId);
        } else {
            //Create ContentValues object to insert data.
            categoryValues = new ContentValues();

            categoryValues.put(AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID, apiId);
            categoryValues.put(AppDataContract.CategoryEntry.COLUMN_CATEGORY_NAME, categoryName);
            categoryValues.put(AppDataContract.CategoryEntry.COLUMN_IMAGE_URL, urlImg);
        }

        categoryCursor.close();
        return categoryValues;
    }




}

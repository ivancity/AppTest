package com.design.ivan.apptest.appsync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.design.ivan.apptest.Constants;
import com.design.ivan.apptest.MainActivity;
import com.design.ivan.apptest.R;
import com.design.ivan.apptest.appdata.AppDataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

    public static final int CATEGORY_NOTIFICATION_ID = 5000;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private static final String[] NOTIFY_CATEGORY_PROJECTION = {
            AppDataContract.CategoryEntry._ID, //important to keep this Column here otherwise it will crash
            AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID,
            AppDataContract.CategoryEntry.COLUMN_CATEGORY_NAME,
            AppDataContract.CategoryEntry.COLUMN_IMAGE_URL,
    };

    static final int INDEX_CATEGORY_ID = 0;
    static final int INDEX_CATEGORY_API_ID = 1;
    static final int INDEX_CATEGORY_NAME = 2;
    static final int INDEX_IMAGE_URL = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CATEGORY_STATUS_OK, CATEGORY_STATUS_SERVER_DOWN,
            CATEGORY_STATUS_SERVER_INVALID, CATEGORY_STATUS_UNKNOWN, CATEGORY_STATUS_INVALID})
    public @interface CategoryStatus {}

    public static final int CATEGORY_STATUS_OK = 0;
    public static final int CATEGORY_STATUS_SERVER_DOWN = 1;
    public static final int CATEGORY_STATUS_SERVER_INVALID = 2;
    public static final int CATEGORY_STATUS_UNKNOWN = 3;
    public static final int CATEGORY_STATUS_INVALID = 4;


    public AppSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "ONPREFORMSYNC Called.");
        //getting necessary URL from extra and pass it to the method
        String rawJsonResponse = getServerData(Constants.category_url);

        try {
            callActivity();
            if(rawJsonResponse != null)
                getCategoryFromJson(rawJsonResponse);
            else
                Log.d(TAG, "Something went wrong receiving raw json response");
        } catch (JSONException e) {
            e.printStackTrace();
            setCategoryStatus(getContext(), CATEGORY_STATUS_SERVER_INVALID);
        } finally {
            notifyUser();
            //TODO: Debug purposes. manage this call back to the Activity properly
            callActivity();
        }
    }

    private void callActivity(){
        getContext().getContentResolver()
                .notifyChange(AppDataContract.CategoryEntry.CONTENT_URI
                        , null
                        , false);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(TAG, "syncing adapter with Content Provider");
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

        if ( null == accountManager.getPassword(newAccount) ) {
            Log.d(TAG, "password doesnt exist in AccountManager");
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

        } else Log.d(TAG, "Password Exists no new Accound added");
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

    /**
     * Initializes the process to verify if we already have an account attach to our Sync Adapter.
     * The Content Resolver will be in charge of setting a Periodic Sync based on our Account,
     * Content Provider and Sync Adapter registered in the manifest. It will generate two types of sync,
     * one precise and one not precise depending of the API that we have on the device.
     * @param context It is needed for having access to the Account Manager from the system.
     */
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Method that generate and sends notifications to the user.
     */
    private void notifyUser(){
        Context context = getContext();

        //Get system preference selected from Settings activity.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String displayNotificationsKey = context.getString(R.string.key_notification_new_message);
        boolean displayNotifications = prefs.getBoolean(displayNotificationsKey,
                Boolean.parseBoolean(context.getString(R.string.pref_notifications_default)));

        String lastNotificationKey = context.getString(R.string.s_prefrence_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        //more than a day has past since last update notify user
//        if(System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS){
            //TODO: make a query to the Content Provider
            //Create URI with specific Id of category
            //Uri categoryUri = AppDataContract.CategoryEntry.buildCategoryUri(SOME_ID);
            //Query Content Provider
/*
            Cursor cursor = context.getContentResolver().query(
                    categoryUri
                    ,NOTIFY_CATEGORY_PROJECTION
                    ,null
                    ,null
                    ,null
            );
*/
            //if(cursor.moveToFirst()){ do something } else { don't do anything }

        Log.d(TAG, "displayNotification = " + displayNotifications);
        if(displayNotifications) {

            //TODO: Get some data from the cursor
            String textMessage = "New Content Received from server";
            int drawableId = android.R.drawable.ic_menu_my_calendar;


            //TODO: create Notifications that expand for API 4.1 >
            // NotificationCompatBuilder is a very convenient way to build backward-compatible
            // notifications.  Just throw in some data.
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                    .setSmallIcon(drawableId)
                    .setContentTitle("Incoming Message")
                    .setContentText(textMessage);

            // Make something interesting happen when the user clicks on the notification.
            // In this case, opening the app is sufficient.
            Intent resultIntent = new Intent(context, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT //If pending intent exist keep it but replace its
                            //extra with new extra coming from this new Pending Intent.
                    );
            //Pending Intent to send when the notification is clicked.
            mBuilder.setContentIntent(resultPendingIntent);

/*
            NotificationManager mNotificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // CATEGORY_NOTIFICATION_ID allows you to update the notification later on.
            //if notification with the same ID has been alaready posted will be replaced with this one.
            mNotificationManager.notify(CATEGORY_NOTIFICATION_ID, mBuilder.build());
*/

            NotificationManagerCompat.from(getContext()).notify(CATEGORY_NOTIFICATION_ID, mBuilder.build());

            //Refresh last sync with current time and save in SharedPreferences.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong(lastNotificationKey, System.currentTimeMillis());
            editor.commit();


        }

    }


    /**
     * Generic http request method to obtain an input stream to return a json string.
     * @param urlServer
     * @return raw JSON string from server
     */
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
                setCategoryStatus(getContext(), CATEGORY_STATUS_SERVER_DOWN);
                return null;
            }
            rawJsonResponse = buffer.toString();
            //Log.d(TAG, rawJsonResponse);

            return rawJsonResponse;


        } catch (IOException e) {
            Log.e("GetCategoryTask", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            setCategoryStatus(getContext(), CATEGORY_STATUS_SERVER_DOWN);
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
        final String MESSAGE_CODE = "cod";

        try{
            String name, imageUrl;
            int id;
            boolean isDuplicated;
            ContentValues categoryValues = null;

            JSONObject categoryJson = new JSONObject(jsonCategoryStr);


            /* do we have an error?
            The next block of code will check if we have an error code coming from the server
            at this point we don't know yet how it is the correct key for code server status
            coming from the server response
             */
            //TODO uncomment when we know what is the correct server code key coming from the JSON string
            /*
            if ( categoryJson.has(MESSAGE_CODE) ) {
                int errorCode = categoryJson.getInt(MESSAGE_CODE);

                switch (errorCode) {
                    case HttpURLConnection.HTTP_OK:
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:
                        setCategoryStatus(getContext(), CATEGORY_STATUS_INVALID);
                        return;
                    default:
                        setCategoryStatus(getContext(), CATEGORY_STATUS_SERVER_DOWN);
                        return;
                }
            }
*/


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

    static private void setCategoryStatus(Context c, @CategoryStatus int categoryStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_category_status_key), categoryStatus);
        spe.commit();
    }



}

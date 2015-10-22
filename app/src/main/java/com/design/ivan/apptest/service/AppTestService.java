package com.design.ivan.apptest.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

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
 * Created by ivanm on 10/21/15.
 */
//TODO: DELETE THIS CLASS
public class AppTestService extends IntentService {


    static final String TAG = AppTestService.class.getSimpleName();
    protected static final String KEY_DATA = "data";
    protected static final String KEY_ID = "id";
    //key for the extra url coming from the calling
    public static final String URL_CATEGORY_EXTRA = "url_category_extra";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AppTestService(String name) {
        super(name);
    }

    public AppTestService(){
        super("AppTest");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "in onHandleIntent");
        //getting necessary URL from extra and pass it to the method
        String rawJsonResponse = getServerData(intent.getStringExtra(URL_CATEGORY_EXTRA));

        try {
            if(rawJsonResponse != null)
                getCategoryFromJson(rawJsonResponse);
            else
                Log.d(TAG, "Something went wrong receiving raw json response");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                inserted = this.getContentResolver().bulkInsert(AppDataContract.CategoryEntry.CONTENT_URI, cvArray);
            }

            Log.d(TAG, "GetCategoryTask Complete. " + inserted + " Inserted");

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

        Cursor categoryCursor = this.getContentResolver().query(
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

    static public class AlarmReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in AlarmReceiver onReceive");
            Intent startIntent = new Intent(context, AppTestService.class);
            //Get the extra coming from the Alarm Manager of the system. Set by the Fragment.
            //Now that the BroacastReceiver is working by the system, wake up the the service.
            startIntent.putExtra(AppTestService.URL_CATEGORY_EXTRA
                    , intent.getStringExtra(AppTestService.URL_CATEGORY_EXTRA));
            context.startService(startIntent);
        }
    }

}

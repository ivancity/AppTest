package com.design.ivan.apptest.appsync;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.design.ivan.apptest.MainActivity;
import com.design.ivan.apptest.R;
import com.design.ivan.apptest.appdata.AppDataContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by ivanm on 10/15/15.
 */
public class GetProductTask extends GetCategoryTask {

    public onFinishGetProduct callBack = null;


    public interface onFinishGetProduct {
        void callBackFromGetProductTask();
    }

    public void setOnFinishGetProductCallback(onFinishGetProduct callbackOnFinish){
        callBack = callbackOnFinish;
    }


    public GetProductTask(Context context) {
        super(context);

        //Activity activity = (Activity) context;
        if(context instanceof MainActivity)
            mTwoPane = true;
        else
            mTwoPane = false;

    }

    private ContentValues addProduct(String apiId, String price, String title, String imgUrl, String imgUrlLong){
        String productId;

        ContentValues productValues = null;

        Cursor productCursor = tContext.getContentResolver().query(
                AppDataContract.ProductEntry.CONTENT_URI,
                new String[]{AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID},
                AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID + " = ?",
                new String[]{apiId},
                null);

        if (productCursor.moveToFirst()) {
            //DEBUG CODE
            //int productIdIndex = productCursor.getColumnIndex(AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID);
            //productId = productCursor.getString(productIdIndex);
            //Log.d(TAG, "ApId from server already exists in the db: " + productId);
        } else {
            //Create ContentValues object to insert data.
            productValues = new ContentValues();

            productValues.put(AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID, apiId);
            productValues.put(AppDataContract.ProductEntry.COLUMN_PRODUCT_TITLE, title);
            productValues.put(AppDataContract.ProductEntry.COLUMN_PRICE, price);
            productValues.put(AppDataContract.ProductEntry.COLUMN_IMAGE_URL, imgUrl);
            productValues.put(AppDataContract.ProductEntry.COLUMN_IMAGE_URL_BIG, imgUrlLong);
        }

        productCursor.close();
        return productValues;

    }

    private void getProductFromJson(String jsonProductStr) throws JSONException{
        final String KEY_PRODUCTS = "products";
        final String KEY_TITLE = "title";
        final String KEY_PRICE = "price";
        final String KEY_CENTS = "cents";
        final String KEY_DEFAULT_IMAGE = "default_image";
        final String KEY_THUMB = "thumb";
        final String KEY_LONG = "long";

        try{

            String title, price, imgUrl, imgUrlLong;
            int id;
            boolean isDuplicated;
            ContentValues productValues = null;

            JSONObject productJson = new JSONObject(jsonProductStr);

            JSONArray productArray = productJson.getJSONObject(KEY_DATA)
                    .getJSONObject(KEY_DATA)
                    .getJSONArray(KEY_PRODUCTS);

            //Vector used for bulkInsert in ContentProvider. It is optimized to work for array of values.
            Vector<ContentValues> cVVector = new Vector<ContentValues>(productArray.length());

            for(int i = 0; i < productArray.length(); i++) {
                JSONObject productObj = productArray.getJSONObject(i);
                id = productObj.getInt(KEY_ID);
                price = String.valueOf(productObj.getJSONObject(KEY_PRICE).getInt(KEY_CENTS));
                title = productObj.getString(KEY_TITLE);
                imgUrl = "http:" + productObj.getJSONObject(KEY_DEFAULT_IMAGE).getString(KEY_THUMB);
                imgUrlLong = "http:" + productObj.getJSONObject(KEY_DEFAULT_IMAGE).getString(KEY_LONG);

                productValues = addProduct(String.valueOf(id), price, title, imgUrl, imgUrlLong);

                if(productValues != null){
                    isDuplicated = hasDuplicateById(productValues.getAsString(AppDataContract.
                            ProductEntry.COLUMN_PRODUCT_API_ID)
                            ,cVVector
                            ,AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID);

                    if(!isDuplicated) {
                        cVVector.add(productValues);
                    }
                }
            }

            int inserted = 0;

            //add to database
            if (cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = tContext.getContentResolver().bulkInsert(AppDataContract.ProductEntry.CONTENT_URI, cvArray);
            }

            Log.d(TAG, "GetProductTask Complete. " + inserted + " Inserted");

        } catch(JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        if(callBack == null && mTwoPane == false)
            createSnack(R.id.coord_layout_product, tContext.getString(R.string.syncing_product_list));
        else
            createSnack(R.id.coord_layout_main, tContext.getString(R.string.syncing_product_list));

    }

    @Override
    protected Void doInBackground(String... params) {

        String rawJsonResponse = getServerData(params[0]);

        try {
            if(rawJsonResponse != null)
                getProductFromJson(rawJsonResponse);
            else
                Log.d(TAG, "Something went wrong receiving raw json response");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //super.onPostExecute(aVoid);

        if(mTwoPane == false) {
            createSnack(R.id.coord_layout_product, tContext.getString(R.string.product_list_synced));
        } else {
            createSnack(R.id.coord_layout_main, tContext.getString(R.string.product_list_synced));
            if(callBack != null)
                callBack.callBackFromGetProductTask();
        }

    }
}

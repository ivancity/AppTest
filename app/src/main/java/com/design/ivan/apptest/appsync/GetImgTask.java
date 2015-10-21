package com.design.ivan.apptest.appsync;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.design.ivan.apptest.appdata.AppDataContract;

/**
 * Created by ivanm on 10/16/15.
 */
public class GetImgTask extends AsyncTask<String, Void, String> {

    private Context tContext;
    private static final int URL_IMG_INDEX = 0;
    private onFinishGetImgTask onFinishCallBack;

    private static final String TAG = GetImgTask.class.getSimpleName();

    public interface onFinishGetImgTask {
        void callBackFromGetImgTask(String imgUrl);
    }

    public void setFinishCallback(onFinishGetImgTask callback){
        onFinishCallBack = callback;
    }


    public GetImgTask(Context context){
        tContext = context;
    }

    @Override
    protected String doInBackground(String... params) {

        String productUrl = getAddressFromDb();

        if(productUrl != null){
            return productUrl;
        } else{
            return null;
        }

    }

    @Override
    protected void onPostExecute(String productUrl) {
        super.onPostExecute(productUrl);
        if(onFinishCallBack != null)
            onFinishCallBack.callBackFromGetImgTask(productUrl);

    }

    private String getAddressFromDb() {
        String productUrl;

        //Query the database using the ContentResolver and get a Cursor with the value if it exists.
        Cursor productCursor = tContext.getContentResolver().query(
                AppDataContract.ProductEntry.buildProductRequestImage("yes"),
                new String[]{AppDataContract.ProductEntry.COLUMN_IMAGE_URL},
                null,
                null,
                AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID);

        if (productCursor.moveToFirst()) {
            //we know that there is only the 0 index because the ContentResolver is querying only
            //one column that will get the index of 0
            productUrl = productCursor.getString(URL_IMG_INDEX);
            Log.d(TAG, "url found: " + productUrl);
        } else {
            //ContentProvider didn't find anythin in the DB.
            productUrl = null;
            Log.d(TAG, "url not found");
        }

        return productUrl;

    }
}

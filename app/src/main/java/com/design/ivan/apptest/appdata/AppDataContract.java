package com.design.ivan.apptest.appdata;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.design.ivan.apptest.BuildConfig;

/**
 * Created by ivanm on 10/13/15.
 */
public class AppDataContract {
    /*
    Here we define the structure of our database, and also the URI's that we are going to use
    to query and request data through our content provider.
    This Data Contract will be very useful to make correct requests to the database. Also to
    refer correctly to columns and tables in the database.
     */
    //name for our content provider
    public static final String CONTENT_AUTHORITY = BuildConfig.AUTHORITY_GRADLE;

    //Base URI for to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //definning possible URIs paths
    //ex: content://com.design.ivan.apptest/category/
    public static final String PATH_CATEGORY = "category";
    public static final String PATH_PRODUCT = "product";


    /**
    Everything to do with Categories detail information
     */
    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        //table to save data regarding category
        public static final String TABLE_NAME = "category";

        //columns for our category table
        public static final String COLUMN_CATEGORY_API_ID = "category_api_id";
        public static final String COLUMN_IMAGE_URL = "img_url";
        public static final String COLUMN_CATEGORY_NAME = "cat_name";

        /*
        Series of URIs that are built for multiple functionalities. In case that it is needed
        to increase the functionality we can work in here. Add more URI builders
         */

        public static Uri buildCategoryUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildCategoryWithName(String apiId, String name){
            return CONTENT_URI.buildUpon()
                    .appendPath(apiId)
                    .appendPath(name).build();
        }

        public static Uri buildCategoryWithNameAndImgUrl(String apidId, String name, String imgUrl){
            return CONTENT_URI.buildUpon()
                    .appendPath(apidId)
                    .appendPath(name)
                    .appendPath(imgUrl).build();
        }

        public static Uri buildCaetgoryWithApiIdQuery(String apiId) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_CATEGORY_API_ID, apiId).build();
        }

        /*
        If we have content://com.design.ivan.apptest/category/name/img_url
        get(0) will return 'category'
        get(1) returns 'name' which is what we want for this method.
         */

        public static String getApiIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getCategoryNameFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getCategoryImgUrlFromUri(Uri uri){
            return uri.getPathSegments().get(3);
        }

    }

    /**
     *
    Everyting to do with Product detail information
     */
    public static final class ProductEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRODUCT).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        //table to save data regarding category
        public static final String TABLE_NAME = "product";

        //columns for our category table
        public static final String COLUMN_PRODUCT_API_ID = "product_api_id";
        public static final String COLUMN_IMAGE_URL = "img_url";
        public static final String COLUMN_IMAGE_URL_BIG = "img_url_big";
        public static final String COLUMN_PRODUCT_TITLE = "product_title";
        public static final String COLUMN_PRICE = "price";

        public static Uri buildProductUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildProductWithTitle(String apiID, String title){
            return CONTENT_URI.buildUpon().appendPath(apiID).appendPath(title).build();
        }

        public static Uri buildProductRequestImage(String someText){
            return CONTENT_URI.buildUpon().appendPath(someText).build();
        }

        public static Uri buildProductWithTitleAndImageUrlAndPrice(String apiId, String title
                , String imgUrl, String price){
            return CONTENT_URI.buildUpon().appendPath(apiId).appendPath(title)
                    .appendPath(imgUrl)
                    .appendPath(title).build();
        }

        public static Uri buildProductWithApiIdQuery(String apiId) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(COLUMN_PRODUCT_API_ID, apiId).build();
        }

        public static Uri buildProductWithApiId(String apiId){
            return CONTENT_URI.buildUpon()
                    .appendPath(apiId).build();
        }

        public static String getApiIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);
        }

        public static String getProductTitleFromUri(Uri uri){
            return uri.getPathSegments().get(2);
        }

        public static String getProductImgUrlFromUri(Uri uri){
            return uri.getPathSegments().get(3);
        }

        public static String getProductPriceFromUri(Uri uri){
            return uri.getPathSegments().get(4);
        }

    }



}

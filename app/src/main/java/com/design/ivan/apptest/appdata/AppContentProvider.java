package com.design.ivan.apptest.appdata;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * This is the Content Provider that the Cursor Loader will use to make calls and references in the
 * apps behalf. And it will request data from the data base. The content provider handles URIs
 * in order to make operations in the data base.
 * Created by ivanm on 10/13/15.
 */
public class AppContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private AppDbHelper mOpenHelper;

    static final int PRODUCT = 100;
    static final int PRODUCT_WITH_APIID = 101;
    static final int PRODUCT_WITH_TITLE_AND_IMGURL_AND_PRICE = 102;

    static final int CATEGORY = 300;
    static final int CATEGORY_WITH_APIID = 301;
    static final int CATEGORY_WITH_NAME_AND_IMGURL = 302;


    private static final SQLiteQueryBuilder appDataCategoryTableQueryBuilder;
    private static final SQLiteQueryBuilder appDataProductTableQueryBuilder;


    //Set the tables that need to be queried.
    //In case that we have more tables in the future please use their Ids and Inner joins as necessary
    static {
        appDataCategoryTableQueryBuilder = new SQLiteQueryBuilder();
        appDataCategoryTableQueryBuilder.setTables(AppDataContract.CategoryEntry.TABLE_NAME);
    }

    static{
        appDataProductTableQueryBuilder = new SQLiteQueryBuilder();
        appDataProductTableQueryBuilder.setTables(AppDataContract.ProductEntry.TABLE_NAME);
    }


    //Set the strings necessary for arguments for querying a given table. Able to expand more in
    //here as well. Depending of the Tables that we have and Inner joins we can query by name,
    //so on. Place the rest of the code here. For now it is querying only to find by some specific
    //id for a category or product.

    //category_api_id = ?
    private static final String categoryApiIdSelection =
            AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID + " = ?";

    //product_api_id = ?
    private static final String productApiIdSelection =
            AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID + " = ?";

    private Cursor getCategoryById(Uri uri, String[] projection, String sortOrder){
        String categoryApiId = AppDataContract.CategoryEntry.getApiIdFromUri(uri);

        //to query use category_api_id = categoryApiId
        return appDataCategoryTableQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                categoryApiIdSelection,
                new String[]{categoryApiId},
                null,
                null,
                sortOrder);
    }

    private Cursor getProductById(Uri uri, String[] projection, String sortOrder){
        String productApiId = AppDataContract.ProductEntry.getApiIdFromUri(uri);

        return appDataProductTableQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                productApiIdSelection,
                new String[]{productApiId},
                null,
                null,
                sortOrder,
                null);
    }



    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AppDataContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, AppDataContract.PATH_PRODUCT, PRODUCT);
        matcher.addURI(authority, AppDataContract.PATH_PRODUCT + "/*", PRODUCT_WITH_APIID);
        //EX: /APIID/TITLE/URLIMAGE/PRICE
        matcher.addURI(authority, AppDataContract.PATH_PRODUCT + "/*/*/*/*", PRODUCT_WITH_TITLE_AND_IMGURL_AND_PRICE);

        matcher.addURI(authority, AppDataContract.PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, AppDataContract.PATH_CATEGORY + "/*", CATEGORY_WITH_APIID);
        //EX: /APPID/CATEGORYNAME/URLIMAGE
        matcher.addURI(authority, AppDataContract.PATH_CATEGORY + "/*/*/*", CATEGORY_WITH_NAME_AND_IMGURL);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new AppDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;

        //We can add more functionality and changes in how we look for data using the URIs set
        //on the AppDataContract and the URIMatcher that we created. There are some exmples of how we
        //can expand functionality. It is not needed for the current development.
        switch (sUriMatcher.match(uri)) {
            case PRODUCT:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AppDataContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case PRODUCT_WITH_APIID:
                retCursor =  mOpenHelper.getReadableDatabase().query(
                        AppDataContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder,
                        "1"
                );
                break;
            //case PRODUCT_WITH_TITLE_AND_IMGURL_AND_PRICE:
              //  break;
            case CATEGORY:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AppDataContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY_WITH_APIID:
                retCursor = getCategoryById(uri, projection, sortOrder);
                break;
            //case CATEGORY_WITH_NAME_AND_IMGURL:
              //  break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //Very important we are inserting a content observer that will watch for changes
        //that happen to that uri and any of their descendants. The Content Provider will easily
        //tell the UI when the cursor changes. Insert or Update operations can affect this.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT:
                return AppDataContract.ProductEntry.CONTENT_TYPE;
            case PRODUCT_WITH_APIID:
                return AppDataContract.ProductEntry.CONTENT_ITEM_TYPE;
            case PRODUCT_WITH_TITLE_AND_IMGURL_AND_PRICE:
                return AppDataContract.ProductEntry.CONTENT_ITEM_TYPE;
            case CATEGORY:
                return AppDataContract.CategoryEntry.CONTENT_TYPE;
            case CATEGORY_WITH_APIID:
                return AppDataContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_WITH_NAME_AND_IMGURL:
                return AppDataContract.CategoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri returnUri;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT:{
                long id = db.insert(AppDataContract.ProductEntry.TABLE_NAME, null, values);
                if(id > 0)
                    returnUri = AppDataContract.ProductEntry.buildProductUri(id);
                else
                    throw new android.database.SQLException("Not able to insert row into: " + uri);
                break;
            }
            case CATEGORY:{
                long id = db.insert(AppDataContract.CategoryEntry.TABLE_NAME, null, values);
                if(id > 0)
                    returnUri = AppDataContract.CategoryEntry.buildCategoryUri(id);
                else
                    throw new android.database.SQLException("Not able to insert row into: " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        //NOTE: pass the incoming uri and NOT the returnUri. Otherwise it will not notify correctly
        //for any of these changes.
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        //if we don't have a selection then delete ALL rows.
        if ( null == selection ) selection = "1";

        switch (match) {
            case PRODUCT:
                rowsDeleted = db.delete(
                        AppDataContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;

            case CATEGORY:
                rowsDeleted = db.delete(
                        AppDataContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Only notify if we have actually deleted some rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        db.close();

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCT:
                rowsUpdated = db.update(AppDataContract.ProductEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            case CATEGORY:
                rowsUpdated = db.update(AppDataContract.CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Only notify if we have actually deleted some rows
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        db.close();
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORY:
                return processBulkData(AppDataContract.CategoryEntry.TABLE_NAME,
                        uri,
                        values);
            case PRODUCT:
                return processBulkData(AppDataContract.ProductEntry.TABLE_NAME,
                        uri,
                        values);
            default:
                return super.bulkInsert(uri, values);
        }
    }

    /**
     * This method will process multiple database transactions. In this case multiple inserts.
     * Once it finishes the for loop it will commit the results when calling transaction successful.
     * @param tableName
     * @param uri
     * @param values
     * @return
     */
    public int processBulkData(String tableName, Uri uri, ContentValues[] values){
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(tableName, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            //you need to setTransactionSuccesful in order for changes to take effect before calling
            //endTransaction
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnCount;
    }

}

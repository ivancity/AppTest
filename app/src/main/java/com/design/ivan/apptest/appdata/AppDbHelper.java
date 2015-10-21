package com.design.ivan.apptest.appdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.design.ivan.apptest.appdata.AppDataContract.CategoryEntry;
import com.design.ivan.apptest.appdata.AppDataContract.ProductEntry;

/**
 * Created by ivanm on 10/13/15.
 */
public class AppDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    //This is done manually and it is important to increase it to reflect the changes in the DB.
    private static final int DATABASE_VERSION = 9;

    static final String DATABASE_NAME = "appdata.db";

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the tables according to what was determined in the AppDataContract. Table names and
     * so on.
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                CategoryEntry._ID + " INTEGER PRIMARY KEY," +
                CategoryEntry.COLUMN_CATEGORY_API_ID + " TEXT UNIQUE NOT NULL, " +
                CategoryEntry.COLUMN_CATEGORY_NAME + " TEXT NOT NULL, " +
                CategoryEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL " +
                ");";

        final String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                ProductEntry._ID + " INTEGER PRIMARY KEY," +
                ProductEntry.COLUMN_PRODUCT_API_ID + " TEXT UNIQUE NOT NULL, " +
                ProductEntry.COLUMN_PRODUCT_TITLE + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_PRICE + " TEXT NOT NULL, " +
                ProductEntry.COLUMN_IMAGE_URL_BIG + " TEXT NOT NULL " +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_CATEGORY_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCT_TABLE);

    }

    /**
     * Because this database is simply a cache of whatever data we get online it is better to
     * drop all tables and start from scratch if something gets modified in the database scheme.
     * We don't need to keep track of the data itself we can always get new data from the internet.
     * @param sqLiteDatabase
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

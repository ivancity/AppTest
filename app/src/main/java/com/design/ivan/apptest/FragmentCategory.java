package com.design.ivan.apptest;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.design.ivan.apptest.appdata.AppDataContract;
import com.design.ivan.apptest.appsync.GetCategoryTask;

/**
 * Created by ivanm on 10/12/15.
 */
public class FragmentCategory extends Fragment
        implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    //RecyclerView categoryRecycleView;

    ListView listCategory;
    AppCursorAdapter adapter;

    private static final int CATEGORY_LOADER = 0;

    private int mPosition = ListView.INVALID_POSITION;
    private static final String KEY_SELECTED = "selected_position";


    private static final String[] CATEGORY_COLUMNS = {
        /*
        This is the projection of the columns that we want back from the CusorLoader. This order
        will be reflected in the ContentProvider request, thus the Cursor that returns will have
        indexes matching exactly this order of columns.
        */
        AppDataContract.CategoryEntry._ID, //important to keep this Column here otherwise it will crash
        AppDataContract.CategoryEntry.COLUMN_CATEGORY_API_ID,
        AppDataContract.CategoryEntry.COLUMN_CATEGORY_NAME,
        AppDataContract.CategoryEntry.COLUMN_IMAGE_URL,
    };

    /*
    This are the indexes that we are going to need in order to access data from a cursor. It has to
    match the order of the CATEGORY_COLUMN. If there is any change in the values above rearrange
    the int indexes below accordingly.
     */
    static final int COL_CATEGORY_ID = 0;
    static final int COL_CATEGORY_API_ID = 1;
    static final int COL_CATEGORY_NAME = 2;
    static final int COL_IMAGE_URL = 3;

    static final String TAG = FragmentCategory.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);


        //TODO: waiting for CursorADapter for RecyclerView
        //categoryRecycleView = (RecyclerView) view.findViewById(R.id.recyclerview_category);
        //TODO: remove listView once CursorAdapter is available for RecyclerView above
        listCategory = (ListView) view.findViewById(R.id.list_category);

        //categoryRecycleView.setHasFixedSize(true);


        /*
        TODO: enable once CursorAdapter for RecyclerView is ready
        categoryRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()
                .getBaseContext()));
        */

        //TODO: Enable when RecyclerAdapter is CursorAdapter ready for the RecyclerView
        //RecyclerAdapter adapter = new RecyclerAdapter(loadTextList());

        adapter = new AppCursorAdapter(getActivity(), null, 0);


        //setting the click listener for the RecyclerView items where it will call onItemClick below
        //adapter.setOnItemClickListener(this);

        //enable later when applying CursorAdapter for RecyclerView
        //categoryRecycleView.setAdapter(adapter);

        //TODO: remove this line when RecyclerAdapter is CursorAdapter ready
        listCategory.setAdapter(adapter);

        listCategory.setOnItemClickListener(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SELECTED)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(KEY_SELECTED);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //the loader manager will take care of the loader life cycle, and some optimization.
        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(KEY_SELECTED, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    protected void updateList(){
        Log.d(TAG, "start category task");
        //It is going to attempt to get the category list from the server
        GetCategoryTask getCategoryTask = new GetCategoryTask(getActivity());
        Activity activity = getActivity();
        getCategoryTask.execute(activity.getString(R.string.category_url));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mPosition = position;
        // CursorAdapter returns a cursor at the correct position for getItem(), or null
        // if it cannot seek to that position.
        Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
        if (cursor != null) {
/*
            ((Callback) getActivity())
                    .onItemSelected(AppDataContract.CategoryEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
*/

            //Using setData to pass data to the next Activity instead of intent extra
            Intent intent = new Intent(getActivity(), ProductActivity.class)
                    .setData(AppDataContract.CategoryEntry.buildCategoryWithNameAndImgUrl(
                            cursor.getString(FragmentCategory.COL_CATEGORY_API_ID),
                            cursor.getString(FragmentCategory.COL_CATEGORY_NAME),
                            cursor.getString(FragmentCategory.COL_IMAGE_URL)
                    ));

            startActivity(intent);

        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //The CursorLoader will call the ContentProvider in our behalf. Thus it will be running
        //in an asynctask in the background with some optimizations than regular AsyncTask.
        Uri categoryListUri = AppDataContract.CategoryEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
                categoryListUri,
                CATEGORY_COLUMNS, //We are specifying the order of the columns thus we can use projection.
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //We have a new cursor with new data coming from the server.
        adapter.swapCursor(cursor);

        if(mPosition != ListView.INVALID_POSITION){
            listCategory.smoothScrollToPosition(mPosition);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}

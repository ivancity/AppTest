package com.design.ivan.apptest;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.design.ivan.apptest.appsync.GetProductTask;
import com.design.ivan.apptest.interfaces.CallBackList;

/**
 * A placeholder fragment containing a simple view. This fragment can be called by any Activity,
 * thus it requires the parent Activity to implement CallBackList interface. This way the
 * fragment will send back to the Activity the onItemClick() event, and let the Activity handle
 * this click.
 */
public class ProductActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>,
                AdapterView.OnItemClickListener{

    AppProductAdapter adapter;
    private ListView productList;

    private int mPosition = ListView.INVALID_POSITION;

    private static final int PRODUCT_LOADER = 1;

    private static final String[] PRODUCT_COLUMNS = {
            AppDataContract.ProductEntry._ID,
            AppDataContract.ProductEntry.COLUMN_PRODUCT_API_ID,
            AppDataContract.ProductEntry.COLUMN_PRODUCT_TITLE,
            AppDataContract.ProductEntry.COLUMN_PRICE,
            AppDataContract.ProductEntry.COLUMN_IMAGE_URL,
            AppDataContract.ProductEntry.COLUMN_IMAGE_URL_BIG
    };

    static final int COL_PRODUCT_ID = 0;
    static final int COL_PRODUCT_API_ID = 1;
    static final int COL_PRODUCT_TITLE = 2;
    static final int COL_PRICE = 3;
    static final int COL_IMAGE_URL = 4;
    static final int COL_IMAGE_URL_BIG = 5;

    static final String TAG = ProductActivityFragment.class.getSimpleName();
    static final String KEY_SELECTED = "selected_position";


    public ProductActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_product2, container, false);

        productList = (ListView)rootView.findViewById(R.id.list_product);

        productList.setOnItemClickListener(this);

        adapter = new AppProductAdapter(getActivity(), null, 0);
        productList.setAdapter(adapter);


        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SELECTED)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(KEY_SELECTED);
        }

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
       // updateList();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != ListView.INVALID_POSITION){
            outState.putInt(KEY_SELECTED, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    protected void updateList(){
        Log.d(TAG, "starting product task");
        GetProductTask getProductTask = new GetProductTask(getActivity());
        getProductTask.execute(getActivity().getString(R.string.product_url));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri productListUri = AppDataContract.ProductEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
                productListUri,
                PRODUCT_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);

        if(mPosition != ListView.INVALID_POSITION){
            //productList.setSelection(mPosition);
            productList.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterProductList, View view, int position, long id) {
        mPosition = position;
        //Get the cursor from a position on list.
        Cursor cursor = (Cursor) adapterProductList.getItemAtPosition(position);
        //if a cursor with data is found then proceed to call the parent Activity. This activity
        //will choose what to do when an item in in the listView is clicked.
        if(cursor != null){
            ((CallBackList)getActivity())
                    .onItemSelected(AppDataContract.ProductEntry
                            .buildProductWithApiId(cursor.getString(COL_PRODUCT_API_ID)));
        }
    }
}

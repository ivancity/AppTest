package com.design.ivan.apptest;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ivanm on 10/20/15.
 */
public class ProductDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private Uri mUri;

    static final String DETAIL_URI = "URI";
    static final String TAG = ProductDetailFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        Bundle arguments = getArguments();
        if(arguments != null)
            mUri = arguments.getParcelable(DETAIL_URI);

        View rootView = inflater.inflate(R.layout.fragment_product_detail, container, false);


        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}

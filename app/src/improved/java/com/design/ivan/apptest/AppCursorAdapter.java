package com.design.ivan.apptest;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by ivanm on 10/14/15.
 */
public class AppCursorAdapter extends CursorAdapter {

    private static final String TAG = AppCursorAdapter.class.getSimpleName();

    public AppCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if(cursor == null)
            return;

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        //NOTE: using here projection values from FragmentCategory. We set this order of indexes
        //already when using CursorLoader thus we know already the indexes for each column.
        String categoryName = cursor.getString(FragmentCategory.COL_CATEGORY_NAME);
        try {

            viewHolder.txtCategoryName.setText(URLDecoder.decode(categoryName, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, "Decodeing string to UTF-8 failed: " + e.toString());
        }
        //txtCategoryName.setText(categoryName);


        Picasso.with(context)
                .load(cursor.getString(FragmentCategory.COL_IMAGE_URL))
                .placeholder(R.drawable.ic_photo_size_select_actual_black_36dp)
                .error(R.drawable.ic_error_black_36dp)
                .into(viewHolder.categoryImage);
    }

    public static class ViewHolder {
        public final ImageView categoryImage;
        public final TextView txtCategoryName;


        public ViewHolder(View view) {
            categoryImage = (ImageView) view.findViewById(R.id.img_category_item);
            txtCategoryName = (TextView) view.findViewById(R.id.txt_category_name);

        }
    }

}

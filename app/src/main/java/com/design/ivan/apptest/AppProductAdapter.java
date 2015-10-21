package com.design.ivan.apptest;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by ivanm on 10/15/15.
 */
public class AppProductAdapter extends CursorAdapter{

    public AppProductAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_list_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if(cursor == null)
            return;

        CardView cardView = (CardView) view;

        String productTitle = cursor.getString(ProductActivityFragment.COL_PRODUCT_TITLE);
        String productPrice = cursor.getString(ProductActivityFragment.COL_PRICE);

        TextView txtTitleView = (TextView) cardView.findViewById(R.id.txt_product_title);
        TextView txtPriceView = (TextView) cardView.findViewById(R.id.txt_product_price);

        txtTitleView.setText(productTitle);
        txtPriceView.setText(productPrice);

        ImageView productImage = (ImageView) cardView.findViewById(R.id.img_product_item);
        Picasso.with(context)
                .load(cursor.getString(ProductActivityFragment.COL_IMAGE_URL))
                .placeholder(R.drawable.ic_photo_size_select_actual_black_36dp)
                .error(R.drawable.ic_error_black_36dp)
                .into(productImage);

    }
}

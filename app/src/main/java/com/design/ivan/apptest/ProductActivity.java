package com.design.ivan.apptest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.design.ivan.apptest.interfaces.CallBackList;

public class ProductActivity extends AppCompatActivity
            implements CallBackList{

    protected static final String DETAIL_FRAGMENT = "detailTAg";
    protected static final String TO_DETAIL = "to_detail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_product_list, new ProductActivityFragment())
                    .commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        Fragment fragment = getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_product_list);

        //check always which Fragment is currently visible. If it is the one with the product list
        //then proceed to update otherwise don't do anything.
        if(fragment instanceof ProductActivityFragment){
            ProductActivityFragment productActivityFragment = (ProductActivityFragment) fragment;
            if(productActivityFragment != null){
                productActivityFragment.updateList();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ProductDetailFragment.DETAIL_URI, uri);

        ProductDetailFragment detailFragment = new ProductDetailFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_product_list, detailFragment, DETAIL_FRAGMENT)
                .addToBackStack(TO_DETAIL)
                .commit();
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}

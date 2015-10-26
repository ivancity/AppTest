package com.design.ivan.apptest;

import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.design.ivan.apptest.appdata.AppDataContract;
import com.design.ivan.apptest.appsync.AppSyncAdapter;
import com.design.ivan.apptest.interfaces.CallBackEmptyList;
import com.design.ivan.apptest.interfaces.CallBackList;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener,
                    CallBackList,
                    CallBackEmptyList {

    protected ViewPager viewPager;

    protected boolean mTwoPane;
    private boolean switchOffSet = false;

    private ContentObserver mObserver;

    private static final String PRODUCTFRAGMENT_TAG = "DFTAG";
    private static final String CATEGORYFRAGMENT_TAG = "CFTAG";
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        if(findViewById(R.id.fragment_product_list) != null) {

            mTwoPane = true;

            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction. If Fragment is DYNAMIC use this code but in this case is STATIC

            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_product_list, new ProductActivityFragment(), PRODUCTFRAGMENT_TAG)
                        .commit();
            }

        } else {
            mTwoPane = false;

            viewPager = (ViewPager) findViewById(R.id.viewpager_main);
            setupViewPager(viewPager);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_main);
            tabLayout.setupWithViewPager(viewPager);

            //This will allow us to handle tab selection and fragment visualization.
            tabLayout.setOnTabSelectedListener(this);
        }

        AppSyncAdapter.initializeSyncAdapter(this);

        //TODO: try using 2 Content Observers
        mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange) {

                Log.d(TAG, "SOMETHING CHANGED in Content Provider");

                if(viewPager != null){
                    //check if listview is still empty
                    FragmentCategory frC = (FragmentCategory)((AppViewPagerAdapter) viewPager.getAdapter())
                            .getItem(0);
                    handleProgressBar(frC);
                } else {
                    //3 pane layout thus find by id
                    FragmentCategory frC = (FragmentCategory)getSupportFragmentManager()
                            .findFragmentById(R.id.fragment_category_list);
                    handleProgressBar(frC);
                }
                /*
                if(switchOffSet) {
                    if(mProgressBar.getVisibility() != View.VISIBLE){
                        mProgressBar.setVisibility(View.VISIBLE);
                        switchOffSet = false;
                    }
                }else {
                    if(mProgressBar.getVisibility() == View.VISIBLE)
                        mProgressBar.setVisibility(View.GONE);
                    else
                        mProgressBar.setVisibility(View.VISIBLE);
                }
                */

            }

            public void handleProgressBar(FragmentCategory frC){
                if(frC != null){
                    if(frC.switchOffSet){
                        frC.providerHasChanged();
                    }
                }
            }

        };

        getContentResolver().registerContentObserver(AppDataContract.CategoryEntry.CONTENT_URI
                , false
                , mObserver);




    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: remove this method when Sync Adapter is implemented for Products
        findProductFragAndUpdateList();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mObserver);
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

    private void findProductFragAndUpdateList(){
        ProductActivityFragment pf = (ProductActivityFragment) getSupportFragmentManager()
                .findFragmentByTag(PRODUCTFRAGMENT_TAG);
        if(pf != null)
            pf.updateList();
    }

    /**
     * Set TabLayout with this ViewPager. Add here all fragments for each of the Tabs, remember
     * to set the title that you want to see for each Tab.
     * @param viewPager view to manage each of the Fragments that go in each tab.
     */
    private void setupViewPager(ViewPager viewPager) {
        AppViewPagerAdapter pagerAdapter = new AppViewPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFrag(new FragmentCategory(), "Page 1");
        pagerAdapter.addFrag(new FragmentProduct(), "Page 2");
        viewPager.setAdapter(pagerAdapter);
    }

    /**
     * important method, we do the switch between fragments here
     * @param tab determines which fragment the viewPager will display.
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onItemSelected(Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ProductDetailFragment.DETAIL_URI, uri);

        ProductDetailFragment detailFragment = new ProductDetailFragment();
        detailFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_product_detail, detailFragment, ProductActivity.DETAIL_FRAGMENT)
                .commit();

        /*
        OR WE HAVE TO OPEN A NEW INTENT AFTER A CLICK WE COULD DO THIS
        Intent intent = new Intent(this, DetailActivity.class)
                          .setData(uri);
        startActivity(intent);
         */

    }

    @Override
    public void onEmptyList(boolean isEmpty) {

    }
}

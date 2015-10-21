package com.design.ivan.apptest;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.design.ivan.apptest.interfaces.CallBackList;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener,
                    CallBackList{

    protected ViewPager viewPager;


    protected boolean mTwoPane;

    private static final String PRODUCTFRAGMENT_TAG = "DFTAG";
    private static final String CATEGORYFRAGMENT_TAG = "CFTAG";

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



    }

    @Override
    protected void onResume() {
        super.onResume();

        findCategoryFragAndUpdateList();
        findProductFragAndUpdateList();

        if(viewPager != null){
            //AppViewPagerAdapter adapter = (AppViewPagerAdapter)viewPager.getAdapter();
            //((FragmentCategory)adapter.getItem(0)).updateList();

        }


    }

    private void findCategoryFragAndUpdateList(){
        FragmentCategory fc = (FragmentCategory) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_category_list);
        if(fc != null)
            fc.updateList();
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
}

package com.design.ivan.apptest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.design.ivan.apptest.appdata.AppDataContract;
import com.design.ivan.apptest.appsync.AppSyncAdapter;
import com.design.ivan.apptest.gcm.RegistrationIntentService;
import com.design.ivan.apptest.interfaces.CallBackEmptyList;
import com.design.ivan.apptest.interfaces.CallBackList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class MainActivity extends AppCompatActivity
        implements TabLayout.OnTabSelectedListener,
                    CallBackList,
                    CallBackEmptyList {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected ViewPager viewPager;

    protected boolean mTwoPane;
    private boolean switchOffSet = false;

    private ContentObserver mObserver;

    private static final String PRODUCTFRAGMENT_TAG = "DFTAG";
    private static final String CATEGORYFRAGMENT_TAG = "CFTAG";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleCloudMessaging mGcm;

    public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";

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


        // If Google Play Services is up to date, we'll want to register GCM. If it is not, we'll
        // skip the registration and this device will not receive any downstream messages from
        // our fake server. Because weather alerts are not a core feature of the app, this should
        // not affect the behavior of the app, from a user perspective.
        if (checkPlayServices()) {
            // Because this is the initial creation of the app, we'll want to be certain we have
            // a token. If we do not, then we will start the IntentService that will register this
            // application with GCM.
            SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(this);
            boolean sentToken = sharedPreferences.getBoolean(SENT_TOKEN_TO_SERVER, false);
            if (!sentToken) {
                Log.d(TAG, "onCreate: About to start intent service");
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        // If Google Play Services is not available, some features, such as GCM-powered weather
        // alerts, will not be available.
        /*
        if (!checkPlayServices()) {
            // Store regID as null
        }
*/
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


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d(TAG, "device is not supported");
            }

            return false;
        }

        return true;



    }

}

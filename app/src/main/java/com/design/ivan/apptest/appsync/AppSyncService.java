package com.design.ivan.apptest.appsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by ivanm on 10/22/15.
 */
public class AppSyncService extends Service {

    private static final Object mSyncAdapterLock = new Object();
    private static AppSyncAdapter mAppSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (mSyncAdapterLock) {
            if (mAppSyncAdapter == null) {
                mAppSyncAdapter = new AppSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAppSyncAdapter.getSyncAdapterBinder();
    }
}

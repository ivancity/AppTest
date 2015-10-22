package com.design.ivan.apptest.appsync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by ivanm on 10/22/15.
 */
public class AccountAuthenticatorService extends Service {

    AccountAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new AccountAuthenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}

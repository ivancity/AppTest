package com.design.ivan.apptest.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by ivanm on 10/21/15.
 */
public class AppTestService extends IntentService {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public AppTestService(String name) {
        super(name);
    }

    public AppTestService(){
        super("AppTest");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}

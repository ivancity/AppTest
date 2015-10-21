package com.design.ivan.apptest;

import android.test.AndroidTestCase;

import com.design.ivan.apptest.appsync.GetCategoryTask;

/**
 * Created by ivanm on 10/14/15.
 */
public class TestGetCategoryTask extends AndroidTestCase {
    public void testAddCategory(){
        GetCategoryTask getCategoryTask = new GetCategoryTask(getContext());
        getCategoryTask.execute("http://public.dawanda.in/category.json");

    }
}

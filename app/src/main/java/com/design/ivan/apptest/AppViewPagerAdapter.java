package com.design.ivan.apptest;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivanm on 10/12/15.
 */
public class AppViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> adapterFragmentList = new ArrayList<>();
    private final List<String> adapterTitleList = new ArrayList<>();

    public AppViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return adapterFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return adapterFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return adapterTitleList.get(position);
    }

    public void addFrag(Fragment fragment, String title) {
        adapterFragmentList.add(fragment);
        adapterTitleList.add(title);
    }

}

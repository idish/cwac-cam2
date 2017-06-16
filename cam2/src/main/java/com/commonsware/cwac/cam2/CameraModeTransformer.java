package com.commonsware.cwac.cam2;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by Idan on 6/15/2017.
 */

public class CameraModeTransformer implements ViewPager.OnPageChangeListener, ViewPager.PageTransformer {

    private ViewPager mViewPager;
    private CameraModesFragmentPagerAdapter mAdapter;
    private CameraController mCtlr;

    public CameraModeTransformer(CameraController ctlr, ViewPager viewPager, CameraModesFragmentPagerAdapter adapter) {
        mViewPager = viewPager;
        viewPager.addOnPageChangeListener(this);
        mAdapter = adapter;
        mCtlr = ctlr;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//        CameraModeFragment currentFragment = mAdapter.getItem(position);
//        if (currentFragment.getArguments().getString("mode").equals("Picture") {
//            mCtlr.getEngine().getBus().post(new CameraFragment.CameraModeChanged(false));
//        }
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void transformPage(View page, float position) {

    }
}

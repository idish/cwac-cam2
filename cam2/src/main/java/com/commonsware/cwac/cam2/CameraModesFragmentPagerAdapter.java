package com.commonsware.cwac.cam2;

import android.app.Fragment;
import android.app.FragmentManager;

import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Idan on 6/15/2017.
 */

public class CameraModesFragmentPagerAdapter extends FragmentPagerAdapter {

        public CameraModesFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

    @Override
    public CameraModeFragment getItem(int position) {
        switch (position) {
            case 0:
                return CameraModeFragment.newInstance(false);
            case 1:
                return CameraModeFragment.newInstance(true);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}

package com.commonsware.cwac.cam2;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Idan on 6/15/2017.
 */

public class CameraModeFragment extends Fragment {

    private Boolean isVideoCameraMode;

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isVideoCameraMode = getArguments().getBoolean("is_video_camera_mode");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cam_mode_switch_frag, container, false);
        TextView cameraModeTxtView = (TextView) v.findViewById(R.id.camera_mode_txt);

        if (isVideoCameraMode) {
            cameraModeTxtView.setText("VIDEO");
        } else {
            cameraModeTxtView.setText("PICTURE");
        }
        return v;
    }

    public static CameraModeFragment newInstance(boolean isVideoCameraMode) {
        CameraModeFragment fragment = new CameraModeFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_video_camera_mode", isVideoCameraMode);
        fragment.setArguments(args);
        return fragment;
    }
}

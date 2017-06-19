package com.commonsware.cwac.cam2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Idan on 6/17/2017.
 */

public class CameraModeSwitcherAdapter extends RecyclerView.Adapter<CameraModeSwitcherAdapter.CameraModeSwitcherViewHolder> {

    public interface CameraModeSwitcherViewClickListener {
        void onClick(int position);
    }

    private CameraModeSwitcherViewClickListener cameraModeSwitcherViewClickListener;

    public void setCameraModeSwitcherViewClickListener(CameraModeSwitcherViewClickListener  listener) {
        this.cameraModeSwitcherViewClickListener = listener;
    }

    @Override
    public CameraModeSwitcherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cam_mode_switcher_adapter, parent, false);
        return new CameraModeSwitcherViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CameraModeSwitcherViewHolder holder, final int position) {
        if (position == 0) {
            holder.cameraModeTxtView.setText("PHOTO");
        } else if (position == 1){
            holder.cameraModeTxtView.setText("VIDEO");
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            cameraModeSwitcherViewClickListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class CameraModeSwitcherViewHolder extends RecyclerView.ViewHolder {

        TextView cameraModeTxtView;

        public CameraModeSwitcherViewHolder(View itemView) {
            super(itemView);
            cameraModeTxtView = (TextView) itemView.findViewById(R.id.camera_mode_txt);
        }
    }
}

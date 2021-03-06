package com.commonsware.cwac.cam2;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
            holder.cameraModeTxtView.setText(R.string.PHOTO);
        } else if (position == 1){
            holder.cameraModeTxtView.setText(R.string.VIDEO);
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
            cameraModeTxtView = itemView.findViewById(R.id.camera_mode_txt);
        }
    }
}

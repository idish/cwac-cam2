package com.commonsware.cwac.cam2;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * Created by Idan on 6/18/2017.
 */

public class PaddingFirstItemDecoration extends RecyclerView.ItemDecoration {
    private final int size;

    public PaddingFirstItemDecoration(int size) {
        this.size = size;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Apply offset only to first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.left += size - view.getWidth() / 2;
        } else if (parent.getChildAdapterPosition(view) == parent.getChildCount() - 1) {
            outRect.right += size - view.getWidth() / 2;
        }
    }
}
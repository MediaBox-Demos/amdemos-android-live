package com.alivc.live.baselive_pull.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

class SpaceItemDecortation extends RecyclerView.ItemDecoration {
    private int space;//声明间距 //使用构造函数定义间距
    private Context mContext;

    public SpaceItemDecortation(int space, Context context) {
        this.space = space;
        this.mContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获得当前item的位置
        int position = parent.getChildAdapterPosition(view);
        if (position == 0) {
            outRect.left = dip2px(mContext, 28);
        }
        outRect.right = this.space;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param dipValue DisplayMetrics类中属性density
     * @return
     */
    private static int dip2px(Context context, float dipValue) {
        if (context == null || context.getResources() == null)
            return 1;
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}

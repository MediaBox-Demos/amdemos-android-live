package com.alivc.live.baselive_pull.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alivc.live.baselive_pull.R;
import com.alivc.live.baselive_pull.adapter.PlayButtonListAdapter;
import com.alivc.live.baselive_pull.listener.ButtonClickListener;

import java.util.List;

/**
 * 底部按钮
 *
 * @author xlx
 */
public class PlayButtonListView extends FrameLayout {
    private RecyclerView mRecyclerView;
    private PlayButtonListAdapter mButtonListAdapter;
    private ButtonClickListener clickListener;
    private boolean isItemsHide = false;

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public PlayButtonListView(@NonNull Context context) {
        this(context, null);
    }

    public PlayButtonListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayButtonListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setData(List<String> data) {
        mButtonListAdapter.setData(data);
    }

    public void hideItems(boolean isItemHide) {
        this.isItemsHide = isItemHide;
        mButtonListAdapter.hideItems(isItemHide);
        if (isItemHide) {
            this.setVisibility(INVISIBLE);
        } else {
            this.setVisibility(VISIBLE);
        }
    }

    public boolean isItemsHide() {
        return isItemsHide;
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.live_button_list, this, true);
        initRecyclerView(view);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.live_button_recycle);
        mButtonListAdapter = new PlayButtonListAdapter();
        LinearLayoutManager manager = new LinearLayoutManager(PlayButtonListView.this.getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mButtonListAdapter);
        mRecyclerView.addItemDecoration(new SpaceItemDecortation(dip2px(getContext(), 28), getContext()));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mButtonListAdapter.setClickListener(new ButtonClickListener() {
            @Override
            public void onButtonClick(String message, int position) {
                if (clickListener != null) {
                    clickListener.onButtonClick(message, position);
                }
            }
        });
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

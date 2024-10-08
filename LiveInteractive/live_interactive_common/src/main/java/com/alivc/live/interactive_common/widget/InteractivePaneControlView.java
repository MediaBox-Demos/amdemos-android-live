package com.alivc.live.interactive_common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.alivc.live.interactive_common.R;

/**
 * @author keria
 * @date 2024/5/28
 * @brief 直播连麦窗格控制操作栏
 */
public class InteractivePaneControlView extends LinearLayout {

    private ImageView mInteractMuteImageView;
    private ImageView mInteractSetEmptyView;
    private ImageView mInteractResizeView;

    private OnClickEventListener mOnClickEventListener;

    private boolean mMuteAudio = false;
    private boolean mEmptyView = false;
    private boolean mResize = false;

    public InteractivePaneControlView(Context context) {
        super(context);
        init(context);
    }

    public InteractivePaneControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InteractivePaneControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_interactive_pane_control, this, true);

        mInteractMuteImageView = findViewById(R.id.iv_mute_audio);
        mInteractSetEmptyView = findViewById(R.id.iv_show_view);
        mInteractResizeView = findViewById(R.id.iv_view_resize);

        setupListeners();
    }

    private void setupListeners() {
        mInteractMuteImageView.setOnClickListener(v -> toggleState(
                () -> mMuteAudio = !mMuteAudio,
                () -> mInteractMuteImageView.setImageResource(mMuteAudio ? R.drawable.ic_interact_mute : R.drawable.ic_interact_unmute),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickMuteAudio(mMuteAudio) : null
        ));

        mInteractSetEmptyView.setOnClickListener(v -> toggleState(
                () -> mEmptyView = !mEmptyView,
                () -> mInteractSetEmptyView.setImageResource(mEmptyView ? R.drawable.ic_view_container_hide : R.drawable.ic_view_container_show),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickEmptyView(mEmptyView) : null
        ));

        mInteractResizeView.setOnClickListener(v -> toggleState(
                () -> mResize = !mResize,
                () -> mInteractResizeView.setImageResource(mResize ? R.drawable.ic_full_screen : R.drawable.ic_half_screen),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickResize(mResize) : null
        ));
    }

    private void toggleState(@Nullable Runnable viewState, @Nullable Runnable viewStateToggler, @Nullable Runnable eventCallback) {
        // 执行状态转换
        if (viewState != null) {
            viewState.run();
        }

        // 更新视图状态
        if (viewStateToggler != null) {
            viewStateToggler.run();
        }

        // 执行事件回调
        if (eventCallback != null) {
            eventCallback.run();
        }
    }

    public void enableMute(boolean mute) {
        if (mInteractMuteImageView != null) {
            mInteractMuteImageView.setVisibility(mute ? View.VISIBLE : View.GONE);
        }
    }

    public void setOnClickEventListener(OnClickEventListener listener) {
        this.mOnClickEventListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mOnClickEventListener = null;
    }

    public interface OnClickEventListener {
        void onClickMuteAudio(boolean mute);

        void onClickEmptyView(boolean empty);

        void onClickResize(boolean resize);
    }
}

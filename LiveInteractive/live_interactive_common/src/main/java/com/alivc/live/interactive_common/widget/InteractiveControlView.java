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
 * @brief 直播连麦控制操作栏
 */
public class InteractiveControlView extends LinearLayout {

    private ImageView mInteractMuteImageView;
    private ImageView mInteractSetEmptyView;

    private OnClickEventListener mOnClickEventListener = null;

    private boolean mMuteAudio = false;
    private boolean mEmptyView = false;

    public InteractiveControlView(Context context) {
        super(context);
        init(context);
    }

    public InteractiveControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InteractiveControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View inflateView = LayoutInflater.from(context).inflate(R.layout.layout_interactive_control, this, true);

        mInteractMuteImageView = inflateView.findViewById(R.id.iv_mute_audio);
        mInteractMuteImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMuteAudio = !mMuteAudio;
                if (mOnClickEventListener != null) {
                    mOnClickEventListener.onClickMuteAudio(mMuteAudio);
                }
                mInteractMuteImageView.setImageResource(mMuteAudio ? R.drawable.ic_interact_mute : R.drawable.ic_interact_not_mute);
            }
        });

        mInteractSetEmptyView = inflateView.findViewById(R.id.iv_show_view);
        mInteractSetEmptyView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEmptyView = !mEmptyView;
                if (mOnClickEventListener != null) {
                    mOnClickEventListener.onClickEmptyView(mEmptyView);
                }
                mInteractSetEmptyView.setImageResource(mEmptyView ? R.drawable.ic_view_container_hide : R.drawable.ic_view_container_show);
            }
        });
    }

    public void enableMute(boolean mute) {
        mInteractMuteImageView.setVisibility(mute ? View.VISIBLE : View.GONE);
    }

    public void initListener(OnClickEventListener listener) {
        mOnClickEventListener = listener;
    }

    public interface OnClickEventListener {
        void onClickMuteAudio(boolean mute);

        void onClickEmptyView(boolean empty);
    }
}

package com.alivc.live.interactive_common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.alivc.live.interactive_common.R;

/**
 * @Author keria
 * @Date 2024/5/28
 * @Brief 直播连麦房间控制操作栏
 */
public class InteractiveRoomControlView extends LinearLayout {
    private ImageView mSwitchCameraIv;
    private ImageView mUseSpeakerPhoneIv;
    private ImageView mMuteAudioIv;
    private ImageView mMuteVideoIv;
    private ImageView mEnableAudioCaptureIv;
    private ImageView mEnableVideoCaptureIv;

    private boolean mUseSpeakerPhone = false;
    private boolean mMuteAudio = false;
    private boolean mMuteVideo = false;
    private boolean mEnableAudioCapture = true;
    private boolean mEnableVideoCapture = true;

    private OnClickEventListener mOnClickEventListener;

    public InteractiveRoomControlView(Context context) {
        super(context);
        init(context);
    }

    public InteractiveRoomControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public InteractiveRoomControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_interactive_room_control, this, true);

        mSwitchCameraIv = findViewById(R.id.iv_camera);
        mUseSpeakerPhoneIv = findViewById(R.id.iv_speaker_phone);
        mMuteAudioIv = findViewById(R.id.iv_mute_audio);
        mMuteVideoIv = findViewById(R.id.iv_mute_video);
        mEnableAudioCaptureIv = findViewById(R.id.iv_enable_audio);
        mEnableVideoCaptureIv = findViewById(R.id.iv_enable_video);

        setupListeners();
    }

    private void setupListeners() {
        mSwitchCameraIv.setOnClickListener(v -> toggleState(
                null,
                null,
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickSwitchCamera() : null
        ));

        mUseSpeakerPhoneIv.setOnClickListener(v -> toggleState(
                () -> mUseSpeakerPhone = !mUseSpeakerPhone,
                () -> mUseSpeakerPhoneIv.setImageResource(mUseSpeakerPhone ? R.drawable.ic_speaker_phone_on : R.drawable.ic_speaker_phone_off),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickSpeakerPhone(mUseSpeakerPhone) : null
        ));

        mMuteAudioIv.setOnClickListener(v -> toggleState(
                () -> mMuteAudio = !mMuteAudio,
                () -> mMuteAudioIv.setImageResource(mMuteAudio ? R.drawable.ic_audio_capture_off : R.drawable.ic_audio_capture_on),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickMuteAudio(mMuteAudio) : null
        ));

        mMuteVideoIv.setOnClickListener(v -> toggleState(
                () -> mMuteVideo = !mMuteVideo,
                () -> mMuteVideoIv.setImageResource(mMuteVideo ? R.drawable.ic_local_video_off : R.drawable.ic_local_video_on),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickMuteVideo(mMuteVideo) : null
        ));

        mEnableAudioCaptureIv.setOnClickListener(v -> toggleState(
                () -> mEnableAudioCapture = !mEnableAudioCapture,
                () -> mEnableAudioCaptureIv.setImageResource(mEnableAudioCapture ? R.drawable.ic_audio_capture_on : R.drawable.ic_audio_capture_off),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickEnableAudio(mEnableAudioCapture) : null
        ));

        mEnableVideoCaptureIv.setOnClickListener(v -> toggleState(
                () -> mEnableVideoCapture = !mEnableVideoCapture,
                () -> mEnableVideoCaptureIv.setImageResource(mEnableVideoCapture ? R.drawable.ic_local_video_on : R.drawable.ic_local_video_off),
                mOnClickEventListener != null ? () -> mOnClickEventListener.onClickEnableVideo(mEnableVideoCapture) : null
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

    public void setOnClickEventListener(OnClickEventListener listener) {
        this.mOnClickEventListener = listener;
    }

    public interface OnClickEventListener {
        void onClickSwitchCamera();

        void onClickSpeakerPhone(boolean enable);

        void onClickMuteAudio(boolean mute);

        void onClickMuteVideo(boolean mute);

        void onClickEnableAudio(boolean enable);

        void onClickEnableVideo(boolean enable);
    }
}

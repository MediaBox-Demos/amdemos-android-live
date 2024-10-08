package com.alivc.live.interactive_common.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alivc.live.interactive_common.R;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.InteractLiveTipsViewListener;
import com.alivc.live.player.annotations.AlivcLivePlayAudioStreamType;
import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;

/**
 * 直播连麦用户信息输入框
 */
public class InteractiveCommonInputView extends ConstraintLayout {

    public enum ViewType {
        INTERACTIVE,
        PK,
        BARE_STREAM
    }

    private ViewType mViewType;

    private final Context mContext;
    private final View inflate;

    private TextView mCancelTextView;
    private TextView mConfirmTextView;

    private TextView mTipsTextView;
    private EditText mInputUserIdEditText;
    private EditText mInputRoomIdEditText;
    private EditText mInputUrlEditText;

    private ImageView mClearUserIdImageView;
    private ImageView mClearRoomIdImageView;
    private ImageView mScanQrImageView;

    private LinearLayout mVideoRadioGroupLayout;
    private RadioGroup mVideoStreamTypeRadioGroup;
    private RadioButton mVideoCamaraRadioButton;
    private RadioButton mVideoScreenRadioButton;

    private LinearLayout mAudioRadioGroupLayout;
    private RadioGroup mAudioStreamTypeRadioGroup;
    private RadioButton mAudioMicRadioButton;
    private RadioButton mAudioDualRadioButton;

    private InteractLiveTipsViewListener mInteractLiveTipsViewListener;

    private boolean mShowInputView;

    public InteractiveCommonInputView(@NonNull Context context) {
        this(context, null);
    }

    public InteractiveCommonInputView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public InteractiveCommonInputView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        inflate = LayoutInflater.from(mContext).inflate(R.layout.layout_interactive_common_input, this, true);
        initView();
        initListener();
    }

    private void initView() {
        mCancelTextView = inflate.findViewById(R.id.tv_cancel);
        mConfirmTextView = inflate.findViewById(R.id.tv_confirm);
        mTipsTextView = inflate.findViewById(R.id.tv_tips);

        mInputUserIdEditText = inflate.findViewById(R.id.et_input_user_id);
        mInputRoomIdEditText = inflate.findViewById(R.id.et_input_room_id);
        mInputUrlEditText = inflate.findViewById(R.id.et_input_url);

        mClearUserIdImageView = inflate.findViewById(R.id.iv_clear_user_id);
        mClearRoomIdImageView = inflate.findViewById(R.id.iv_clear_room_id);
        mScanQrImageView = inflate.findViewById(R.id.iv_scan_qr);

        mVideoRadioGroupLayout = inflate.findViewById(R.id.video_radio_group);
        mVideoStreamTypeRadioGroup = inflate.findViewById(R.id.rg_video_stream_type);
        mVideoCamaraRadioButton = inflate.findViewById(R.id.rb_camera);
        mVideoScreenRadioButton = inflate.findViewById(R.id.rb_screen);

        mAudioRadioGroupLayout = inflate.findViewById(R.id.audio_radio_group);
        mAudioStreamTypeRadioGroup = inflate.findViewById(R.id.rg_audio_stream_type);
        mAudioMicRadioButton = inflate.findViewById(R.id.rb_mic);
        mAudioDualRadioButton = inflate.findViewById(R.id.rb_dual);
    }

    private void initListener() {
        mCancelTextView.setOnClickListener(view -> {
            if (mInteractLiveTipsViewListener != null) {
                mInteractLiveTipsViewListener.onCancel();
            }
        });

        mConfirmTextView.setOnClickListener(view -> {
            if (mInteractLiveTipsViewListener == null) {
                return;
            }
            if (!mShowInputView) {
                mInteractLiveTipsViewListener.onConfirm();
                return;
            }
            InteractiveUserData inputData = new InteractiveUserData();
            if (mViewType == ViewType.INTERACTIVE) {
                inputData.userId = mInputUserIdEditText.getText().toString().trim();
            } else if (mViewType == ViewType.PK) {
                inputData.userId = mInputUserIdEditText.getText().toString().trim();
                inputData.channelId = mInputRoomIdEditText.getText().toString().trim();
            } else if (mViewType == ViewType.BARE_STREAM) {
                inputData.url = mInputUrlEditText.getText().toString().trim();
            }
            inputData.videoStreamType = (mVideoStreamTypeRadioGroup.getCheckedRadioButtonId() == mVideoCamaraRadioButton.getId())
                    ? AlivcLivePlayVideoStreamType.STREAM_CAMERA
                    : AlivcLivePlayVideoStreamType.STREAM_SCREEN;
            inputData.audioStreamType = (mAudioStreamTypeRadioGroup.getCheckedRadioButtonId() == mAudioMicRadioButton.getId())
                    ? AlivcLivePlayAudioStreamType.STREAM_MIC
                    : AlivcLivePlayAudioStreamType.STREAM_DUAL;
            mInteractLiveTipsViewListener.onInputConfirm(inputData);
        });

        mClearUserIdImageView.setOnClickListener(view -> mInputUserIdEditText.setText(""));
        mClearRoomIdImageView.setOnClickListener(view -> mInputRoomIdEditText.setText(""));
        mScanQrImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInteractLiveTipsViewListener != null) {
                    mInteractLiveTipsViewListener.onQrClick();
                }
            }
        });
    }

    public void setViewType(ViewType viewType) {
        mViewType = viewType;

        if (viewType == ViewType.INTERACTIVE) {
            mInputUserIdEditText.setVisibility(View.VISIBLE);
            mClearUserIdImageView.setVisibility(View.VISIBLE);
            mInputRoomIdEditText.setVisibility(View.GONE);
            mClearRoomIdImageView.setVisibility(View.GONE);
            mInputUrlEditText.setVisibility(View.GONE);
            mScanQrImageView.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mAudioRadioGroupLayout.setVisibility(View.VISIBLE);
            mVideoRadioGroupLayout.setVisibility(View.VISIBLE);
        } else if (viewType == ViewType.PK) {
            mInputUserIdEditText.setVisibility(View.VISIBLE);
            mClearUserIdImageView.setVisibility(View.VISIBLE);
            mInputRoomIdEditText.setVisibility(View.VISIBLE);
            mClearRoomIdImageView.setVisibility(View.VISIBLE);
            mInputUrlEditText.setVisibility(View.GONE);
            mScanQrImageView.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mAudioRadioGroupLayout.setVisibility(View.VISIBLE);
            mVideoRadioGroupLayout.setVisibility(View.VISIBLE);
        } else if (viewType == ViewType.BARE_STREAM) {
            mInputUserIdEditText.setVisibility(View.GONE);
            mInputRoomIdEditText.setVisibility(View.GONE);
            mClearUserIdImageView.setVisibility(View.GONE);
            mClearRoomIdImageView.setVisibility(View.GONE);
            mInputUrlEditText.setVisibility(View.VISIBLE);
            mScanQrImageView.setVisibility(View.VISIBLE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mAudioRadioGroupLayout.setVisibility(View.GONE);
            mVideoRadioGroupLayout.setVisibility(View.GONE);
        }
    }

    public void setDefaultRoomId(String roomId) {
        if (!TextUtils.isEmpty(roomId)) {
            mInputRoomIdEditText.setText(roomId);
        }
    }

    public void showInputView(String content, boolean showInputView) {
        this.mShowInputView = showInputView;
        if (!mShowInputView) {
            mInputUserIdEditText.setVisibility(View.GONE);
            mInputRoomIdEditText.setVisibility(View.GONE);
            mInputUrlEditText.setVisibility(View.GONE);
            mClearUserIdImageView.setVisibility(View.GONE);
            mClearRoomIdImageView.setVisibility(View.GONE);
            mTipsTextView.setVisibility(View.VISIBLE);
            mTipsTextView.setText(content);
            mAudioRadioGroupLayout.setVisibility(View.GONE);
            mVideoRadioGroupLayout.setVisibility(View.GONE);
        } else {
            setViewType(mViewType);
        }
    }

    public void setQrResult(String content) {
        mInputUrlEditText.setText(content);
    }

    public void setOnInteractLiveTipsViewListener(InteractLiveTipsViewListener listener) {
        this.mInteractLiveTipsViewListener = listener;
    }
}

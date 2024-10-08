package com.alivc.live.interactive_live;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.commonui.messageview.AutoScrollMessagesView;
import com.alivc.live.commonui.utils.StatusBarUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.interactive_common.InteractiveConstants;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.ConnectionLostListener;
import com.alivc.live.interactive_common.listener.InteractLivePushPullListener;
import com.alivc.live.interactive_common.listener.InteractLiveTipsViewListener;
import com.alivc.live.interactive_common.utils.InteractLiveIntent;
import com.alivc.live.commonui.avdialog.AUILiveDialog;
import com.alivc.live.interactive_common.widget.ConnectionLostTipsView;
import com.alivc.live.interactive_common.widget.InteractiveCommonInputView;
import com.alivc.live.interactive_common.widget.InteractiveConnectView;
import com.alivc.live.interactive_common.widget.InteractiveRoomControlView;
import com.alivc.live.interactive_common.widget.MultiAlivcLiveView;
import com.alivc.live.player.annotations.AlivcLivePlayError;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Route(path = "/interactiveLive/multiInteractLive")
public class MultiInteractLiveActivity extends AppCompatActivity {
    private AUILiveDialog mAUILiveDialog;

    //Dialog 弹窗的意图
    private InteractLiveIntent mCurrentIntent;
    private ImageView mCloseImageView;
    private TextView mShowConnectIdTextView;
    //大窗口
    private FrameLayout mBigFrameLayout;
    private SurfaceView mBigSurfaceView;
    private TextView mHomeIdTextView;
    private InteractiveUserData mAnchorUserData;
    private MultiAnchorController mMultiAnchorController;
    //根据 TextView 获取其他 View
    private final Map<TextView, MultiAlivcLiveView> mViewCombMap = new HashMap<>();
    //根据 id 获取其他 View
    private final Map<String, MultiAlivcLiveView> mIdViewCombMap = new HashMap<>();
    private ConnectionLostTipsView mConnectionLostTipsView;
    private InteractiveRoomControlView mInteractiveRoomControlView;
    private InteractiveConnectView mInteractiveConnectView1;
    private InteractiveConnectView mInteractiveConnectView2;
    private InteractiveConnectView mInteractiveConnectView3;
    private InteractiveConnectView mInteractiveConnectView4;
    private AutoScrollMessagesView mSeiMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        StatusBarUtil.translucent(this, Color.TRANSPARENT);

        setContentView(R.layout.activity_multi_interact_live);

        initView();
        initListener();
        initData();
    }

    private void initData() {
        InteractiveUserData anchorUserData = (InteractiveUserData) getIntent().getSerializableExtra(InteractiveConstants.DATA_TYPE_INTERACTIVE_USER_DATA);
        mAnchorUserData = anchorUserData;

        mMultiAnchorController = new MultiAnchorController(this, anchorUserData);
        mMultiAnchorController.setMultiInteractLivePushPullListener(new InteractLivePushPullListener() {
            @Override
            public void onPullSuccess(InteractiveUserData userData) {
                super.onPullSuccess(userData);
                if (userData == null) {
                    return;
                }
                String viewKey = userData.getKey();
                MultiAlivcLiveView multiAlivcLiveView = mIdViewCombMap.get(viewKey);
                if (multiAlivcLiveView != null) {
                    changeSmallSurfaceViewVisible(true, multiAlivcLiveView);
                    updateConnectTextView(true, multiAlivcLiveView.getConnectTextView());
                }
            }

            @Override
            public void onPullError(InteractiveUserData userData, AlivcLivePlayError errorType, String errorMsg) {
                super.onPullError(userData, errorType, errorMsg);
                runOnUiThread(() -> {
                    if (userData == null) {
                        return;
                    }
                    String viewKey = userData.getKey();
                    mMultiAnchorController.stopConnect(viewKey);
                    MultiAlivcLiveView multiAlivcLiveView = mIdViewCombMap.get(viewKey);
                    if (multiAlivcLiveView != null) {
                        changeSmallSurfaceViewVisible(false, multiAlivcLiveView);
                        updateConnectTextView(false, multiAlivcLiveView.getConnectTextView());
                    }
                    ToastUtils.show(getResources().getString(R.string.interact_live_viewer_left));
                });
            }

            @Override
            public void onConnectionLost() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectionLostTipsView.show();
                    }
                });
            }

            @Override
            public void onReceiveSEIMessage(int payload, byte[] data) {
                super.onReceiveSEIMessage(payload, data);
                mSeiMessageView.appendMessage("[rtc] payload=" + payload + ", " + new String(data, StandardCharsets.UTF_8));
            }

            @Override
            public void onReceiveSEIDelay(String src, String type, String msg) {
                super.onReceiveSEIDelay(src, type, msg);
                mSeiMessageView.appendMessage("[" + src + "][" + type + "][" + msg + "ms]");
            }
        });
        mMultiAnchorController.setAnchorRenderView(mBigFrameLayout);
        mMultiAnchorController.startPush();

        mHomeIdTextView.setText(anchorUserData.channelId);
    }

    private void initView() {
        mAUILiveDialog = new AUILiveDialog(this);
        mInteractiveConnectView1 = findViewById(R.id.connect_view_1);
        mInteractiveConnectView2 = findViewById(R.id.connect_view_2);
        mInteractiveConnectView3 = findViewById(R.id.connect_view_3);
        mInteractiveConnectView4 = findViewById(R.id.connect_view_4);

        mBigSurfaceView = findViewById(R.id.big_surface_view);
        mCloseImageView = findViewById(R.id.iv_close);
        mShowConnectIdTextView = findViewById(R.id.tv_show_connect);
        mBigFrameLayout = findViewById(R.id.big_fl);
        mInteractiveRoomControlView = findViewById(R.id.interactive_setting_view);
        mSeiMessageView = findViewById(R.id.sei_receive_view);

        //小窗口
        FrameLayout mSmallFrameLayout1 = findViewById(R.id.small_fl_1);
        FrameLayout mSmallFrameLayout2 = findViewById(R.id.small_fl_2);
        FrameLayout mSmallFrameLayout3 = findViewById(R.id.small_fl_3);
        FrameLayout mSmallFrameLayout4 = findViewById(R.id.small_fl_4);

        mHomeIdTextView = findViewById(R.id.tv_home_id);

        mConnectionLostTipsView = new ConnectionLostTipsView(this);

        mBigSurfaceView.setZOrderOnTop(true);
        mBigSurfaceView.setZOrderMediaOverlay(true);

        MultiAlivcLiveView multiAlivcLiveView1 = new MultiAlivcLiveView(mInteractiveConnectView1.getConnectFrameLayout(), mSmallFrameLayout1, mInteractiveConnectView1.getConnectTextView());
        MultiAlivcLiveView multiAlivcLiveView2 = new MultiAlivcLiveView(mInteractiveConnectView2.getConnectFrameLayout(), mSmallFrameLayout2, mInteractiveConnectView2.getConnectTextView());
        MultiAlivcLiveView multiAlivcLiveView3 = new MultiAlivcLiveView(mInteractiveConnectView3.getConnectFrameLayout(), mSmallFrameLayout3, mInteractiveConnectView3.getConnectTextView());
        MultiAlivcLiveView multiAlivcLiveView4 = new MultiAlivcLiveView(mInteractiveConnectView4.getConnectFrameLayout(), mSmallFrameLayout4, mInteractiveConnectView4.getConnectTextView());

        mViewCombMap.put(mInteractiveConnectView1.getConnectTextView(), multiAlivcLiveView1);
        mViewCombMap.put(mInteractiveConnectView2.getConnectTextView(), multiAlivcLiveView2);
        mViewCombMap.put(mInteractiveConnectView3.getConnectTextView(), multiAlivcLiveView3);
        mViewCombMap.put(mInteractiveConnectView4.getConnectTextView(), multiAlivcLiveView4);
    }

    private void initListener() {
        mInteractiveRoomControlView.setOnClickEventListener(new InteractiveRoomControlView.OnClickEventListener() {
            @Override
            public void onClickSwitchCamera() {
                mMultiAnchorController.switchCamera();
            }

            @Override
            public void onClickSpeakerPhone(boolean enable) {
                mMultiAnchorController.enableSpeakerPhone(enable);
            }

            @Override
            public void onClickMuteAudio(boolean mute) {
                mMultiAnchorController.setMute(mute);
            }

            @Override
            public void onClickMuteVideo(boolean mute) {
                mMultiAnchorController.muteLocalCamera(mute);
            }

            @Override
            public void onClickEnableAudio(boolean enable) {
                mMultiAnchorController.enableAudioCapture(enable);
            }

            @Override
            public void onClickEnableVideo(boolean enable) {
                mMultiAnchorController.enableLocalCamera(enable);
            }
        });

        mConnectionLostTipsView.setConnectionLostListener(new ConnectionLostListener() {
            @Override
            public void onConfirm() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });

        //开始连麦
        mInteractiveConnectView1.setConnectClickListener(() -> {
            clickStartConnect(mInteractiveConnectView1.getConnectTextView());
        });
        mInteractiveConnectView2.setConnectClickListener(() -> {
            clickStartConnect(mInteractiveConnectView2.getConnectTextView());
        });
        mInteractiveConnectView3.setConnectClickListener(() -> {
            clickStartConnect(mInteractiveConnectView3.getConnectTextView());
        });
        mInteractiveConnectView4.setConnectClickListener(() -> {
            clickStartConnect(mInteractiveConnectView4.getConnectTextView());
        });

        mCloseImageView.setOnClickListener(view -> {
            mCurrentIntent = InteractLiveIntent.INTENT_FINISH;
            showInteractLiveDialog(null, getResources().getString(R.string.interact_live_leave_room_tips), false);
        });
    }

    private void clickStartConnect(TextView mCurrentTextView) {
        if (mCurrentTextView != null && mCurrentTextView.getTag() != null && mMultiAnchorController.isOnConnected(mCurrentTextView.getTag().toString())) {
            //主播端停止连麦
            mCurrentIntent = InteractLiveIntent.INTENT_STOP_PULL;
            showInteractLiveDialog(mCurrentTextView, getResources().getString(R.string.interact_live_connect_finish_tips), false);
        } else {
            //主播端开始连麦，输入用户 id
            showInteractLiveDialog(mCurrentTextView, getResources().getString(R.string.interact_live_connect_tips), true);
        }
    }

    private void changeSmallSurfaceViewVisible(boolean isShowSurfaceView, MultiAlivcLiveView alivcLiveView) {
        alivcLiveView.getSmallFrameLayout().setVisibility(isShowSurfaceView ? View.VISIBLE : View.INVISIBLE);
        alivcLiveView.getUnConnectFrameLayout().setVisibility(isShowSurfaceView ? View.INVISIBLE : View.VISIBLE);
    }

    public void updateConnectTextView(boolean connecting, TextView mConnectTextView) {
        if (connecting) {
            mShowConnectIdTextView.setVisibility(View.VISIBLE);
            mConnectTextView.setText(getResources().getString(R.string.interact_stop_connect));
            mConnectTextView.setBackground(getResources().getDrawable(R.drawable.shape_interact_live_un_connect_btn_bg));
        } else {
            mShowConnectIdTextView.setVisibility(View.GONE);
            mConnectTextView.setText(getResources().getString(R.string.interact_start_connect));
            mConnectTextView.setBackground(getResources().getDrawable(R.drawable.shape_pysh_btn_bg));
            mConnectTextView.setTag("");
        }
    }

    private void showInteractLiveDialog(TextView textView, String content, boolean showInputView) {
        InteractiveCommonInputView commonInputView = new InteractiveCommonInputView(MultiInteractLiveActivity.this);
        commonInputView.setViewType(InteractiveCommonInputView.ViewType.INTERACTIVE);
        commonInputView.showInputView(content, showInputView);
        mAUILiveDialog.setContentView(commonInputView);
        mAUILiveDialog.show();

        commonInputView.setOnInteractLiveTipsViewListener(new InteractLiveTipsViewListener() {
            @Override
            public void onCancel() {
                if (mAUILiveDialog.isShowing()) {
                    mAUILiveDialog.dismiss();
                }
            }

            @Override
            public void onConfirm() {
                if (mCurrentIntent == InteractLiveIntent.INTENT_STOP_PULL) {
                    //主播结束连麦
                    mAUILiveDialog.dismiss();
                    mMultiAnchorController.stopConnect((String) textView.getTag());
                    updateConnectTextView(false, textView);
                    MultiAlivcLiveView multiAlivcLiveView = mViewCombMap.get(textView);
                    if (multiAlivcLiveView != null) {
                        changeSmallSurfaceViewVisible(false, multiAlivcLiveView);
                    }
                } else if (mCurrentIntent == InteractLiveIntent.INTENT_FINISH) {
                    finish();
                }
            }

            @Override
            public void onInputConfirm(InteractiveUserData userData) {
                hideInputSoftFromWindowMethod(MultiInteractLiveActivity.this, commonInputView);
                if (userData == null || TextUtils.isEmpty(userData.userId)) {
                    ToastUtils.show(getResources().getString(R.string.interact_live_connect_input_error_tips));
                    return;
                }
                userData.channelId = mAnchorUserData != null ? mAnchorUserData.channelId : "";
                userData.url = AliLiveStreamURLUtil.generateInteractivePullUrl(userData.channelId, userData.userId);
                mAUILiveDialog.dismiss();
                //每个观众对应一个 AlvcLivePlayer
                String viewKey = userData.getKey();
                if (textView != null) {
                    textView.setTag(viewKey);
                }
                mIdViewCombMap.put(viewKey, mViewCombMap.get(textView));
                //主播端，输入观众 id 后，开始连麦
                mMultiAnchorController.startConnect(userData, Objects.requireNonNull(mViewCombMap.get(textView)).getSmallFrameLayout());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMultiAnchorController.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMultiAnchorController.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMultiAnchorController.release();
    }

    public void hideInputSoftFromWindowMethod(Context context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
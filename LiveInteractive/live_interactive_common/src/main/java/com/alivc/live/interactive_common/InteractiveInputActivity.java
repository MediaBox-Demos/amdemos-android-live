package com.alivc.live.interactive_common;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.acker.simplezxing.activity.CaptureActivity;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.alivc.live.commonbiz.backdoor.BackDoorInstance;
import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.commonui.utils.StatusBarUtil;
import com.alivc.live.commonutils.TextFormatUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.utils.LivePushGlobalConfig;
import com.alivc.live.interactive_common.widget.InteractLiveRadioButton;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author keria
 * @date 2024/5/29
 * @brief 互动直播房间号/用户ID输入页面
 */
@Route(path = "/interactiveCommon/liveInput")
public class InteractiveInputActivity extends AppCompatActivity {

    private EditText mUserIdEdt;
    private EditText mRoomIdEdt;

    private ImageView mUserIdClearIv;
    private ImageView mRoomIdClearIv;

    private TextView mURLEntranceTv;
    private TextView mInteractiveURLTv;

    private TextView mSettingTv;
    private TextView mConfirmTv;
    private ImageView mBackIv;

    // 直播连麦场景下的主播/观众角色选取
    private InteractLiveRadioButton mAnchorRb;
    private InteractLiveRadioButton mAudienceRb;

    // 场景类型，PK直播 or 连麦直播
    private int mSceneType = InteractiveConstants.SCENE_TYPE_INTERACTIVE_LIVE;
    private boolean isAnchor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.translucent(this, Color.TRANSPARENT);

        setContentView(R.layout.activity_interactive_input);

        // 获取场景类型
        mSceneType = getIntent().getIntExtra(InteractiveConstants.DATA_TYPE_SCENE, InteractiveConstants.SCENE_TYPE_INTERACTIVE_LIVE);

        initView();
        initListener();
    }

    private void initView() {
        mUserIdEdt = findViewById(R.id.et_user_id);
        mRoomIdEdt = findViewById(R.id.et_room_id);
        mUserIdClearIv = findViewById(R.id.iv_user_id_clear);
        mRoomIdClearIv = findViewById(R.id.iv_room_id_clear);

        mURLEntranceTv = findViewById(R.id.tv_url_entrance);
        mInteractiveURLTv = findViewById(R.id.tv_interactive_url);

        // Update page title
        TextView titleTv = findViewById(R.id.tv_title);
        titleTv.setText(mSceneType == InteractiveConstants.SCENE_TYPE_PK_LIVE ? getString(R.string.pk_live) : getString(R.string.interact_live));

        mSettingTv = findViewById(R.id.tv_setting);
        mBackIv = findViewById(R.id.iv_back);
        mConfirmTv = findViewById(R.id.tv_confirm);

        View roleContainer = findViewById(R.id.role_container);
        roleContainer.setVisibility(mSceneType == InteractiveConstants.SCENE_TYPE_PK_LIVE ? View.GONE : View.VISIBLE);

        mAnchorRb = findViewById(R.id.radio_button_anchor);
        mAnchorRb.setChecked(true);
        mAudienceRb = findViewById(R.id.radio_button_audience);
    }

    private void initListener() {
        InteractLiveAppInfoFragment interactLiveAppInfoFragment = (InteractLiveAppInfoFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_app_info);
        if (interactLiveAppInfoFragment != null) {
            interactLiveAppInfoFragment.setOnEditClickListener(new InteractLiveAppInfoFragment.OnEditClickListener() {
                @Override
                public void onLongClickAppInfoForDebug() {
                    InteractiveBaseUtil.generateRandomId(mSceneType, (channelId, userId) -> {
                        if (TextUtils.isEmpty(mRoomIdEdt.getText())) {
                            mRoomIdEdt.setText(channelId);
                        }
                        if (TextUtils.isEmpty(mUserIdEdt.getText())) {
                            mUserIdEdt.setText(userId);
                        }
                    });
                }

                @Override
                public void onEditClickListener() {
                    Intent intent = new Intent(InteractiveInputActivity.this, InteractiveAppInfoActivity.class);
                    intent.putExtra(InteractiveAppInfoActivity.FROM_EDITOR, true);
                    intent.putExtra(InteractiveConstants.DATA_TYPE_SCENE, mSceneType);
                    startActivity(intent);
                }
            });
        }

        mBackIv.setOnClickListener(view -> finish());

        mURLEntranceTv.setOnClickListener(v -> {
            startCaptureActivityForResult();
        });

        mSettingTv.setOnClickListener(view -> {
            Intent intent = new Intent(this, InteractiveSettingActivity.class);
            startActivity(intent);
        });

        mUserIdEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                changeConfirmTextView(checkEnable());
            }
        });

        mRoomIdEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                changeConfirmTextView(checkEnable());
            }
        });

        mAnchorRb.setOnClickListener(view -> {
            isAnchor = true;
            mAnchorRb.setChecked(true);
            mAudienceRb.setChecked(false);
        });

        mAudienceRb.setOnClickListener(view -> {
            isAnchor = false;
            mAnchorRb.setChecked(false);
            mAudienceRb.setChecked(true);
        });

        mUserIdClearIv.setOnClickListener(view -> mUserIdEdt.setText(""));
        mRoomIdClearIv.setOnClickListener(view -> mRoomIdEdt.setText(""));

        mConfirmTv.setOnClickListener(view -> {
            if (checkEnable()) {
                startInteractiveActivity();
            }
        });
    }

    private void changeConfirmTextView(boolean enable) {
        if (enable) {
            mConfirmTv.setBackground(getResources().getDrawable(R.drawable.shape_pysh_btn_bg));
        } else {
            mConfirmTv.setBackground(getResources().getDrawable(R.drawable.shape_rect_blue));
        }
    }

    private boolean checkEnable() {
        String url = mInteractiveURLTv.getText().toString();
        if (!TextUtils.isEmpty(url)) {
            return true;
        }

        String userId = mUserIdEdt.getText().toString();
        String roomId = mRoomIdEdt.getText().toString();
        return !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(roomId) && Pattern.matches(TextFormatUtil.REGULAR, userId) && Pattern.matches(TextFormatUtil.REGULAR, roomId);
    }

    private void startInteractiveActivity() {
        String schema;
        if (mSceneType == InteractiveConstants.SCENE_TYPE_PK_LIVE) {
            schema = LivePushGlobalConfig.IS_MULTI_INTERACT
                    ? "/interactivePK/multiPKLive"
                    : "/interactivePK/pkLive";
        } else {
            schema = LivePushGlobalConfig.IS_MULTI_INTERACT
                    ? "/interactiveLive/multiInteractLive"
                    : "/interactiveLive/interactLive";
        }

        ARouter.getInstance()
                .build(schema)
                .withSerializable(InteractiveConstants.DATA_TYPE_INTERACTIVE_USER_DATA, generateUserData())
                .withBoolean(InteractiveConstants.DATA_TYPE_IS_ANCHOR, isAnchor)
                .withBoolean(InteractiveConstants.KEY_TYPE_SCENE_MULTI_PK_16IN, BackDoorInstance.getInstance().isUseMultiPK16IN())
                .navigation();
    }

    private void startCaptureActivityForResult() {
        Intent intent = new Intent(InteractiveInputActivity.this, CaptureActivity.class);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE) {
            if (resultCode == RESULT_OK) {
                String pushUrl = data.getStringExtra(CaptureActivity.EXTRA_SCAN_RESULT);
                if (TextUtils.isEmpty(pushUrl)) {
                    return;
                }
                mInteractiveURLTv.setText(pushUrl);
                clearInputUserInfo(true);
                changeConfirmTextView(checkEnable());
                checkPushURLSDKAppID(pushUrl);
            }
        }
    }

    // 清除输入的用户信息
    private void clearInputUserInfo(boolean useUrl) {
        if (useUrl) {
            mUserIdEdt.setText("");
            mRoomIdEdt.setText("");
        } else {
            mInteractiveURLTv.setText("");
        }
    }

    // 如果当前以URL方式输入的 sdkAppId 和 demo 当前环境使用的 sdkAppId 不一致，则提示 toast，以免业务异常
    private void checkPushURLSDKAppID(String pushURL) {
        if (TextUtils.isEmpty(pushURL)) {
            return;
        }
        HashMap<String, String> params = AliLiveStreamURLUtil.parseUrl(pushURL);
        String sdkAppId = params.get(AliLiveStreamURLUtil.SDK_APP_ID);
        if (!TextUtils.equals(sdkAppId, AliLiveStreamURLUtil.APP_ID)) {
            ToastUtils.show(getString(R.string.tips_interactive_url_use_different_sdkappid));
        }
    }

    /**
     * 生成 user data，里面存有房间号，用户id，实际推流地址等信息
     *
     * @return user data
     */
    private InteractiveUserData generateUserData() {
        String channelId = mRoomIdEdt.getText().toString();
        String userId = mUserIdEdt.getText().toString();
        String pushURL = mInteractiveURLTv.getText().toString();

        InteractiveUserData userData = new InteractiveUserData();
        if (!TextUtils.isEmpty(channelId) && !TextUtils.isEmpty(userId)) {
            userData.userId = userId;
            userData.channelId = channelId;
            userData.url = AliLiveStreamURLUtil.generateInteractivePushUrl(channelId, userId);
        } else if (!TextUtils.isEmpty(pushURL)) {
            HashMap<String, String> params = AliLiveStreamURLUtil.parseUrl(pushURL);
            userData.channelId = AliLiveStreamURLUtil.parseURLStreamName(params);
            userData.userId = params.get(AliLiveStreamURLUtil.USER_ID);
            userData.url = pushURL;
        }
        return userData;
    }
}
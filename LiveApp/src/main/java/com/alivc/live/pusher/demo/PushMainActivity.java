package com.alivc.live.pusher.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alivc.live.annotations.AlivcLiveMode;
import com.alivc.live.commonui.activity.AUILiveBaseListActivity;
import com.alivc.live.baselive_pull.ui.PlayerActivity;
import com.alivc.live.baselive_pull_rts.InputRtsUrlActivity;
import com.alivc.live.baselive_push.ui.PushConfigActivity;
import com.alivc.live.baselive_recording.ui.VideoRecordConfigActivity;
import com.alivc.live.commonbiz.SharedPreferenceUtils;
import com.alivc.live.commonbiz.test.PushDemoTestConstants;
import com.alivc.live.commonutils.ContextUtils;
import com.alivc.live.commonutils.FastClickUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.interactive_common.InteractiveAppInfoActivity;
import com.alivc.live.interactive_common.InteractiveConstants;
import com.alivc.live.interactive_common.InteractiveInputActivity;
import com.alivc.live.pusher.AlivcLiveBase;

import java.util.ArrayList;
import java.util.List;

@Route(path = "/live/MainActivity")
public class PushMainActivity extends AUILiveBaseListActivity {
    private static final int INDEX_CAMERA_PUSH = 0;
    private static final int INDEX_SCREEN_PUSH = 1;
    private static final int INDEX_LOCAL_VIDEO_PUSH = 2;
    private static final int INDEX_PULL = 3;
    private static final int INDEX_INTERACT_LIVE = 4;
    private static final int INDEX_INTERACT_PK_LIVE = 5;
    private static final int INDEX_RTS_PULL = 6;
    private final int PERMISSION_REQUEST_CODE = 1;
    private String[] permissionManifest;
    private int mObPermissionsIndex = -1;

    @Override
    public int getTitleResId() {
        return R.string.push_title_name;
    }

    @Override
    public boolean showBackBtn() {
        return true;
    }

    @Override
    public List<ListModel> createListData() {
        List<ListModel> menu = new ArrayList<>();
        menu.add(new ListModel(INDEX_CAMERA_PUSH, R.drawable.ic_live_push, getResources().getString(R.string.push_enter_name_tv), getResources().getString(R.string.push_enter_name_desc)));
        menu.add(new ListModel(INDEX_SCREEN_PUSH, R.drawable.ic_live_player_luping, getResources().getString(R.string.pull_common_enter_name_tv), getResources().getString(R.string.pull_common_enter_name_desc)));
//        menu.add(new ListModel(INDEX_LOCAL_VIDEO_PUSH, R.drawable.ic_live_bendi, getResources().getString(R.string.push_local_video_name_tv), "本期忽略"));
        menu.add(new ListModel(INDEX_PULL, R.drawable.ic_live_player_laliu, getResources().getString(R.string.pull_rtc_enter_name_tv), getResources().getString(R.string.pull_rtc_enter_name_desc)));
        if (AlivcLiveBase.isSupportLiveMode(AlivcLiveMode.AlivcLiveInteractiveMode)) {
            menu.add(new ListModel(INDEX_RTS_PULL, R.drawable.ic_live_player_laliu, getResources().getString(R.string.pull_rts_enter_name), ""));
            menu.add(new ListModel(INDEX_INTERACT_LIVE, R.drawable.ic_live_interact, getResources().getString(R.string.interact_live), getResources().getString(R.string.interact_live)));
            menu.add(new ListModel(INDEX_INTERACT_PK_LIVE, R.drawable.ic_pk_interact, getResources().getString(R.string.pk_live), getResources().getString(R.string.pk_live)));
        }
        return menu;
    }

    @Override
    public void onListItemClick(ListModel model) {
        if (FastClickUtil.isProcessing()) {
            return;
        }
        if (!permissionCheck()) {
            if (mObPermissionsIndex >= 0) {
                ToastUtils.show("请手动开启" + noPermissionTip[mObPermissionsIndex] + "权限");
            }

            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            }
            return;
        }


        switch (model.index) {
            case INDEX_CAMERA_PUSH:
                startActivity(new Intent(this, PushConfigActivity.class));

                break;
            case INDEX_SCREEN_PUSH:
                startActivity(new Intent(this, VideoRecordConfigActivity.class));
                break;
            case INDEX_LOCAL_VIDEO_PUSH:
                //0615本期不做
                break;
            case INDEX_PULL:
                startActivity(new Intent(this, PlayerActivity.class));
                break;
            case INDEX_INTERACT_LIVE:
                gotoNextActivityWithScene(InteractiveConstants.SCENE_TYPE_INTERACTIVE_LIVE);
                break;
            case INDEX_INTERACT_PK_LIVE:
                gotoNextActivityWithScene(InteractiveConstants.SCENE_TYPE_PK_LIVE);
                break;
            case INDEX_RTS_PULL:
                startActivity(new Intent(this, InputRtsUrlActivity.class));
                break;
            default:
                break;
        }
    }

    private void gotoNextActivityWithScene(int scene) {
        Intent intent;
        if (checkInteractiveAPPInfoIfNeed()) {
            intent = new Intent(PushMainActivity.this, InteractiveAppInfoActivity.class);
            intent.putExtra(InteractiveConstants.DATA_TYPE_SCENE, scene);
        } else {
            intent = new Intent(PushMainActivity.this, InteractiveInputActivity.class);
            intent.putExtra(InteractiveConstants.DATA_TYPE_SCENE, scene);
        }
        startActivity(intent);
    }

    private final String[] permissionManifest23 = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
    };


    private final String[] permissionManifest31 = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_CONNECT,
    };

    private final String[] permissionManifest33 = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.BLUETOOTH_CONNECT,
    };

    private final int[] noPermissionTip = {
            R.string.no_camera_permission,
            R.string.no_record_audio_permission,
            R.string.no_read_phone_state_permission,
            R.string.no_write_external_storage_permission,
            R.string.no_read_external_storage_permission,
            R.string.no_read_bluetooth_connect_permission
    };

    private boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionManifest = permissionManifest33;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionManifest = permissionManifest31;
        } else {
            permissionManifest = permissionManifest23;
        }
        int permissionCheck = PermissionChecker.PERMISSION_GRANTED;
        String permission;
        for (int i = 0; i < permissionManifest.length; i++) {
            permission = permissionManifest[i];
            int state = PermissionChecker.checkSelfPermission(this, permission);
            if (state == PermissionChecker.PERMISSION_DENIED) {
                permissionCheck = PermissionChecker.PERMISSION_DENIED;
            } else if (state == PermissionChecker.PERMISSION_DENIED_APP_OP) {
                mObPermissionsIndex = i;
                permissionCheck = PermissionChecker.PERMISSION_DENIED_APP_OP;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkInteractiveAPPInfoIfNeed() {
        Context context = ContextUtils.getApplicationContext();

        // 如果sp里面appInfo都有，返回false，不需要填写
        if (!(TextUtils.isEmpty(SharedPreferenceUtils.getAppId(context))
                || TextUtils.isEmpty(SharedPreferenceUtils.getAppKey(context))
                || TextUtils.isEmpty(SharedPreferenceUtils.getPlayDomain(context)))) {
            return false;
        }

        // 如果开发调试用的appInfo都有，返回false，不需要填写
        String testAppID = PushDemoTestConstants.getTestInteractiveAppID();
        String testAppKey = PushDemoTestConstants.getTestInteractiveAppKey();
        String testPlayDomain = PushDemoTestConstants.getTestInteractivePlayDomain();
        if (!(PushDemoTestConstants.checkIsPlaceholder(testAppID)
                || PushDemoTestConstants.checkIsPlaceholder(testAppKey)
                || PushDemoTestConstants.checkIsPlaceholder(testPlayDomain))) {
            // 将开发调试用的appInfo写入到sp里面
            SharedPreferenceUtils.setAppInfo(getApplicationContext(), testAppID, testAppKey, testPlayDomain);
            return false;
        }

        return true;
    }
}
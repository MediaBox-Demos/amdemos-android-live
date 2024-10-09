package com.alivc.live.commonbiz.backdoor;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alivc.live.commonbiz.R;
import com.alivc.live.commonbiz.SharedPreferenceUtils;
import com.alivc.live.commonui.widgets.LivePushTextSwitch;
import com.alivc.live.commonutils.AppUtil;
import com.alivc.live.commonutils.FileUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.pusher.AlivcLiveBase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author keria
 * @date 2023/10/13
 * @brief
 */
public class BackDoorActivity extends AppCompatActivity {

    private static final String SD_CARD_LOG_SUFFIX = "device-info.log";

    private TextView mInfoTv;

    private LivePushTextSwitch mForceRTCPreEnvSw;
    private LivePushTextSwitch mMultiPK16InSw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.push_activity_backdoor);
        initViews();
        initData();
    }

    private void initViews() {
        mForceRTCPreEnvSw = findViewById(R.id.sw_force_rtc_pre_env);
        mForceRTCPreEnvSw.setTextViewText(getString(R.string.backdoor_rtc_force_pre_env));
        mForceRTCPreEnvSw.setOnSwitchToggleListener(isChecked -> {
            if (BackDoorInstance.getInstance().isForceRTCPreEnvironment() != isChecked) {
                BackDoorInstance.getInstance().setForceRTCPreEnvironment(isChecked);
                // 清除当前连麦配置信息
                SharedPreferenceUtils.setAppInfo(getApplicationContext(), "", "", "");
                // 退出APP
                killApp();
            }
        });

        mMultiPK16InSw = findViewById(R.id.sw_multi_pk_16in);
        mMultiPK16InSw.setTextViewText(getString(R.string.backdoor_multi_pk_16in));
        mMultiPK16InSw.setOnSwitchToggleListener(isChecked -> {
            if (BackDoorInstance.getInstance().isUseMultiPK16IN() != isChecked) {
                BackDoorInstance.getInstance().setUseMultiPK16IN(isChecked);
            }
        });

        mInfoTv = findViewById(R.id.info_tv);
        StringBuffer sb = new StringBuffer();
        sb.append(getDemoBuildInfo());
        sb.append("\n\n");
        sb.append(getSdkBuildInfo());
        sb.append("\n\n");
        sb.append(AppUtil.getBriefDeviceInfo());
        sb.append("\n\n");
        sb.append(AppUtil.getDeviceInfo());
        sb.append("\n\n");
        sb.append(AppUtil.getFullCPUInfo());
        sb.append("\n\n");
        sb.append(AppUtil.getOpenGLESInfo(this));
        sb.append("\n\n");
        mInfoTv.setText(sb.toString());
        mInfoTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyInfo2Clipboard();
                dumpInfo2SdCard();
                return true;
            }
        });
    }

    private void initData() {
        mForceRTCPreEnvSw.setSwitchChecked(BackDoorInstance.getInstance().isForceRTCPreEnvironment());
        mMultiPK16InSw.setSwitchChecked(BackDoorInstance.getInstance().isUseMultiPK16IN());
    }

    private static String getDemoBuildInfo() {
        return String.format("%s%n%s%n%s%n%s",
                "Demo Build Type: " + com.alivc.live.commonbiz.BuildConfig.BUILD_TYPE,
                "Demo Build AIO: " + com.alivc.live.commonbiz.BuildConfig.MTL_BUILD_FOR_AIO,
                "Demo Build ID: " + com.alivc.live.commonbiz.BuildConfig.MTL_BUILD_ID,
                "Demo Build Timestamp: " + com.alivc.live.commonbiz.BuildConfig.MTL_BUILD_TIMESTAMP
        );
    }

    private static String getSdkBuildInfo() {
        return String.format("%s%n%s%n%s%n%s%n%s%n%s",
                "SDK Version: " + AlivcLiveBase.getSDKVersion(),
                "SDK Build Type: " + com.alivc.live.pusher.BuildConfig.BUILD_TYPE,
                "SDK Build Interactive: " + com.alivc.live.pusher.BuildConfig.BUILD_INTERACTIVE,
                "SDK Pre Environment: " + com.alivc.live.pusher.BuildConfig.PUSH_SDK_PRE_ENV,
                "SDK Head Commit ID: " + com.alivc.live.pusher.BuildConfig.HEAD_COMMIT_ID,
                "SDK Build ID: " + com.alivc.live.pusher.BuildConfig.MTL_BUILD_ID,
                "SDK Build Timestamp: " + com.alivc.live.pusher.BuildConfig.MTL_BUILD_TIMESTAMP
        );
    }

    private void copyInfo2Clipboard() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            return;
        }
        cm.setText(mInfoTv.getText());
        ToastUtils.show("copy to clipboard success!");
    }

    private void dumpInfo2SdCard() {
        removeOldSdCardLogs();
        String folderPath = getSdCardLogPath();
        String filename = String.format("%s-%s", new SimpleDateFormat("yyMMddHHmmss").format(new Date()), SD_CARD_LOG_SUFFIX);
        String finalPath = FileUtil.combinePaths(folderPath, filename);

        FileWriter fr = null;
        try {
            fr = new FileWriter(finalPath);
            fr.write(mInfoTv.getText().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeOldSdCardLogs() {
        String folderPath = getSdCardLogPath();
        ArrayList<File> files = FileUtil.listAllFiles(new File(folderPath), false);
        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                String path = file.getAbsolutePath();
                if (path.endsWith(SD_CARD_LOG_SUFFIX)) {
                    FileUtil.safeDeleteFile(path);
                }
            }
        }
    }

    private String getSdCardLogPath() {
        String parentPath = getExternalFilesDir("").getAbsolutePath();
        String folderPath = FileUtil.combinePaths(parentPath, "Ali_RTS_Log");
        FileUtil.safeCreateFolder(folderPath);
        return folderPath;
    }

    /**
     * 强制退出APP
     */
    private static void killApp() {
        try {
            Thread.sleep(2 * 1000L);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

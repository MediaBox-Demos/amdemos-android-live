package com.alivc.live.pusher.demo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alivc.live.commonbiz.BuildConfig;
import com.alivc.live.commonutils.AssetUtil;
import com.alivc.live.commonutils.FileUtil;
import com.alivc.live.pusher.AlivcLiveBase;
import com.alivc.live.pusher.AlivcLiveBaseListener;
import com.alivc.live.pusher.AlivcLivePushConstants;
import com.alivc.live.pusher.AlivcLivePushLogLevel;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by keria on 2022/10/12.
 * <p>
 * 直播推流启动类
 *
 * @see <a href="https://www.aliyun.com/product/live">阿里云视频直播官网</a>
 * <p>
 * @see <a href="https://help.aliyun.com/zh/live/developer-reference/integrate-a-push-sdk-license">推流SDK License文档</a>
 * @see <a href="https://help.aliyun.com/zh/live/user-guide/co-streaming/">直播连麦文档</a>
 * @see <a href="https://help.aliyun.com/zh/live/developer-reference/push-sdk/">推流SDK文档</a>
 */
public class PushLaunchManager {
    private static final String TAG = PushLaunchManager.class.getSimpleName();

    private static final ExecutorService mWorkerThread = Executors.newSingleThreadExecutor();

    private PushLaunchManager() {
    }

    /**
     * 启动所有任务
     *
     * @param context android context
     */
    public static void launch4All(@NonNull Context context) {
        launch4LivePushDemo(context);
        registerPushSDKLicense();
        launch4Log(context);
    }

    /**
     * Demo Asset加载等
     *
     * @param context android context
     */
    private static void launch4LivePushDemo(@NonNull Context context) {
        mWorkerThread.execute(() -> {
            String targetPath = FileUtil.getInternalFilesFolder(context.getApplicationContext()) + File.separator + "alivc_resource";
            AssetUtil.copyAssetToSdCard(context.getApplicationContext(), "alivc_resource", targetPath);
        });
    }

    /**
     * 推流SDK申请License
     *
     * @see <a href="https://help.aliyun.com/zh/live/developer-reference/integrate-a-push-sdk-license">推流SDK License文档</a>
     */
    private static void registerPushSDKLicense() {
        AlivcLiveBase.setListener(new AlivcLiveBaseListener() {
            @Override
            public void onLicenceCheck(AlivcLivePushConstants.AlivcLiveLicenseCheckResultCode alivcLiveLicenseCheckResultCode, String s) {
                Log.e(TAG, "onLicenceCheck: " + alivcLiveLicenseCheckResultCode + ", " + s);
            }
        });
        // 注册推流SDK
        AlivcLiveBase.registerSDK();
    }

    /**
     * 推流SDK日志配置
     * <p>
     * 在debug环境下，使用console日志；release环境下，使用file日志；
     *
     * @param context android context
     * @note ADB pull file logs cmd: adb pull /sdcard/Android/data/com.alivc.live.pusher.demo/files/ ~/Downloads/live-logs
     */
    private static void launch4Log(@NonNull Context context) {
        AlivcLiveBase.setLogLevel(AlivcLivePushLogLevel.AlivcLivePushLogLevelInfo);
        AlivcLiveBase.setConsoleEnabled(!BuildConfig.DEBUG);

        String logPath = getLogFilePath(context.getApplicationContext(), "alivc_live");
        // Log file size limit, total = kLogMaxFileSizeInKB * 5 (parts)
        int maxPartFileSizeInKB = 100 * 1024 * 1024; // Limit to 100MB for each log part

        // Set up log directory path
        AlivcLiveBase.setLogDirPath(logPath, maxPartFileSizeInKB);
    }

    private static String getLogFilePath(@NonNull Context context, String dir) {
        String logFilePath;

        // Check if the external storage is mounted
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            logFilePath = context.getExternalFilesDir(dir).getAbsolutePath();
        } else {
            // Fallback to internal storage if external storage is not available
            logFilePath = new File(context.getFilesDir(), dir).getAbsolutePath();
        }

        // Ensure the directory exists
        File file = new File(logFilePath);
        if (!file.exists() && !file.mkdirs()) {
            Log.e(TAG, "Failed to create log directory: " + logFilePath);
        }

        Log.d(TAG, "Log file path: " + logFilePath);
        return logFilePath;
    }
}

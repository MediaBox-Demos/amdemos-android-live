package com.alivc.live.interactive_common;

import static com.alivc.live.interactive_common.utils.LivePushGlobalConfig.mAlivcLivePushConfig;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.alivc.component.custom.AlivcLivePushCustomFilter;
import com.alivc.live.annotations.AlivcLiveAudioFrameObserverOperationMode;
import com.alivc.live.annotations.AlivcLiveAudioFrameObserverUserDefinedInfoBitMask;
import com.alivc.live.annotations.AlivcLiveAudioSource;
import com.alivc.live.annotations.AlivcLiveMode;
import com.alivc.live.annotations.AlivcLiveNetworkQuality;
import com.alivc.live.annotations.AlivcLivePushKickedOutType;
import com.alivc.live.annotations.AlivcLiveRecordMediaEvent;
import com.alivc.live.beauty.BeautyFactory;
import com.alivc.live.beauty.BeautyInterface;
import com.alivc.live.beauty.constant.BeautySDKType;
import com.alivc.live.commonbiz.seidelay.SEIDelayManager;
import com.alivc.live.commonbiz.seidelay.SEISourceType;
import com.alivc.live.commonbiz.seidelay.api.ISEIDelayEventListener;
import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.commonutils.ToastUtils;
import com.alivc.live.interactive_common.bean.InteractiveLivePlayer;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.InteractLivePushPullListener;
import com.alivc.live.interactive_common.manager.TimestampWatermarkManager;
import com.alivc.live.interactive_common.utils.LivePushGlobalConfig;
import com.alivc.live.player.AlivcLivePlayConfig;
import com.alivc.live.player.AlivcLivePlayer;
import com.alivc.live.player.annotations.AlivcLivePlayError;
import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;
import com.alivc.live.pusher.AlivcAudioChannelEnum;
import com.alivc.live.pusher.AlivcAudioSampleRateEnum;
import com.alivc.live.pusher.AlivcImageFormat;
import com.alivc.live.pusher.AlivcLiveAudioEffectConfig;
import com.alivc.live.pusher.AlivcLiveAudioFrameObserverConfig;
import com.alivc.live.pusher.AlivcLiveBase;
import com.alivc.live.pusher.AlivcLiveLocalRecordConfig;
import com.alivc.live.pusher.AlivcLiveMixStream;
import com.alivc.live.pusher.AlivcLivePublishState;
import com.alivc.live.pusher.AlivcLivePushAudioFrame;
import com.alivc.live.pusher.AlivcLivePushAudioFrameListener;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushExternalAudioStreamConfig;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcLivePusherRawDataSample;
import com.alivc.live.pusher.AlivcLiveTranscodingConfig;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcSoundFormat;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 直播连麦基础类
 *
 * @note 请参考 API 文档，了解对应接口的释义，辅助接入
 * @note 互动模式下，请使用`AlivcRTC`作为日志tag，进行自主排障。
 */
public class InteractLiveBaseManager {

    static {
        // 1. 注册推流SDK
        AlivcLiveBase.registerSDK();
    }

    protected static final String TAG = "InteractLiveBaseManager";

    protected InteractiveMode mInteractiveMode;

    // 这个TM用来做混流的
    protected FrameLayout mAudienceFrameLayout;

    //多人连麦混流
    protected final ArrayList<AlivcLiveMixStream> mMultiInteractLiveMixStreamsArray = new ArrayList<>();

    //多人连麦 Config
    protected final AlivcLiveTranscodingConfig mMixInteractLiveTranscodingConfig = new AlivcLiveTranscodingConfig();

    protected Context mContext;

    protected AlivcLivePusher mAlivcLivePusher;
    private InteractiveUserData mPushUserData;

    protected final Map<String, InteractiveLivePlayer> mInteractiveLivePlayerMap = new HashMap<>();
    protected final Map<String, InteractiveUserData> mInteractiveUserDataMap = new HashMap<>();

    protected InteractLivePushPullListener mInteractLivePushPullListener;

    // 美颜处理类，需对接Queen美颜SDK，且拥有License拥有美颜能力
    private BeautyInterface mBeautyManager;
    // CameraId，美颜需要使用
    private int mCameraId;

    // 互动模式下的水印处理类
    private TimestampWatermarkManager mTimestampWatermarkManager;

    protected final SEIDelayManager mSEIDelayManager = new SEIDelayManager();

    public void init(Context context, InteractiveMode interactiveMode) {
        mInteractiveMode = interactiveMode;
        mContext = context.getApplicationContext();
        initLivePusher(interactiveMode);
        initWatermarks();
        mSEIDelayManager.registerReceiver(new ISEIDelayEventListener() {
            @Override
            public void onEvent(String src, String type, String msg) {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onReceiveSEIDelay(src, type, msg);
                }
            }
        });
    }

    /**
     * 2. 初始化 AlivcLivePushConfig 和 AlivcLivePusher
     *
     * @param interactiveMode 直播连麦模式（直播连麦、RTS 2.0推拉裸流）
     */
    private void initLivePusher(InteractiveMode interactiveMode) {
        // 初始化推流配置类

        // 设置推流模式为 AlivcLiveInteractiveMode，即：互动模式；互动直播，支持连麦、PK等实时互动
        mAlivcLivePushConfig.setLivePushMode(AlivcLiveMode.AlivcLiveInteractiveMode);
        // 互动模式下，如需与Web连麦互通，必须使用H5兼容模式，否则，Web用户查看Native用户将是黑屏。
        mAlivcLivePushConfig.setH5CompatibleMode(LivePushGlobalConfig.IS_H5_COMPATIBLE);

        // 互动模式下开启RTS推拉裸流(直推&直拉，不同于直播连麦)
        if (InteractiveMode.isBareStream(interactiveMode)) {
            mAlivcLivePushConfig.setEnableRTSForInteractiveMode(true);
        }

        /// 如果是外部音视频推流，需要初始化的 Pusher 配置，以及自定义音频、视频的Profiles
        if (mAlivcLivePushConfig.isExternMainStream()) {
            mAlivcLivePushConfig.setExternMainStream(true, AlivcImageFormat.IMAGE_FORMAT_YUVNV12, AlivcSoundFormat.SOUND_FORMAT_S16);
            mAlivcLivePushConfig.setAudioChannels(AlivcAudioChannelEnum.AUDIO_CHANNEL_ONE);
            mAlivcLivePushConfig.setAudioSampleRate(AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_44100);
        }

        // 如果需要开启 Data Channel 自定义消息通道，以代替 SEI 功能，需要预先开启 Data Channel 自定义消息通道
        if (LivePushGlobalConfig.IS_DATA_CHANNEL_MESSAGE_ENABLE) {
            mAlivcLivePushConfig.setEnableDataChannelMessage(true);
        }

        // 初始化推流引擎
        mAlivcLivePusher = new AlivcLivePusher();
        mAlivcLivePusher.init(mContext.getApplicationContext(), mAlivcLivePushConfig);

        // 设置音量回调频率和平滑系数
        mAlivcLivePusher.enableAudioVolumeIndication(300, 3, 1);

        /*
         * 错误异常及特殊场景处理
         * 参考文档：https://help.aliyun.com/zh/live/developer-reference/handling-of-exceptions-and-special-scenarios-for-android
         */

        // 设置推流错误事件
        mAlivcLivePusher.setLivePushErrorListener(new AlivcLivePushErrorListener() {
            @Override
            public void onSystemError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {
                Log.e(TAG, "onSystemError: " + alivcLivePushError);
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushError();
                }
            }

            @Override
            public void onSDKError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {
                Log.e(TAG, "onSDKError: " + alivcLivePushError);
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushError();
                }
            }
        });

        // 设置推流通知事件
        mAlivcLivePusher.setLivePushInfoListener(new AlivcLivePushInfoListener() {
            @Override
            public void onPreviewStarted(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPreviewStarted: ");
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushSuccess();
                }
            }

            @Override
            public void onPreviewStopped(AlivcLivePusher pusher) {
                Log.d(TAG, "onPreviewStopped: ");
            }

            @Override
            public void onPushStarted(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPushStarted: ");
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushSuccess();
                }
            }

            @Override
            public void onPushPaused(AlivcLivePusher pusher) {
                Log.d(TAG, "onPushPaused: ");
            }

            @Override
            public void onPushResumed(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPushResumed: ");
            }

            @Override
            public void onPushStopped(AlivcLivePusher pusher) {
                Log.d(TAG, "onPushStopped: ");
                pusher.stopPreview();
            }

            @Override
            public void onPushRestarted(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPushRestarted: ");
            }

            @Override
            public void onFirstFramePushed(AlivcLivePusher pusher) {
                Log.d(TAG, "onFirstFramePushed: ");
            }

            @Override
            public void onFirstFramePreviewed(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onFirstFramePreviewed: ");
            }

            @Override
            public void onDropFrame(AlivcLivePusher alivcLivePusher, int i, int i1) {
                Log.d(TAG, "onDropFrame: ");
            }

            @Override
            public void onAdjustBitrate(AlivcLivePusher pusher, int currentBitrate, int targetBitrate) {
                Log.i(TAG, "onAdjustBitrate: " + currentBitrate + "->" + targetBitrate);
            }

            @Override
            public void onAdjustFps(AlivcLivePusher alivcLivePusher, int i, int i1) {
                Log.d(TAG, "onAdjustFps: ");
            }

            @Override
            public void onPushStatistics(AlivcLivePusher alivcLivePusher, AlivcLivePushStatsInfo alivcLivePushStatsInfo) {
                // Log.i(TAG, "onPushStatistics: " + alivcLivePushStatsInfo);
            }

            @Override
            public void onSetLiveMixTranscodingConfig(AlivcLivePusher alivcLivePusher, boolean b, String s) {
                Log.d(TAG, "onSetLiveMixTranscodingConfig: ");
            }

            @Override
            public void onKickedOutByServer(AlivcLivePusher pusher, AlivcLivePushKickedOutType kickedOutType) {
                Log.d(TAG, "onKickedOutByServer: " + kickedOutType);
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushError();
                }
            }

            @Override
            public void onMicrophoneVolumeUpdate(AlivcLivePusher pusher, int volume) {
                // 麦克风音量回调（仅互动模式下生效，需设置AlivcLivePusher#enableAudioVolumeIndication接口）
                // Log.d(TAG, "onMicrophoneVolumeUpdate: " + volume);
            }

            @Override
            public void onLocalRecordEvent(AlivcLiveRecordMediaEvent mediaEvent, String storagePath) {
                ToastUtils.show(mediaEvent + ", " + storagePath);
            }

            @Override
            public void onRemoteUserEnterRoom(AlivcLivePusher pusher, String userId, boolean isOnline) {
                ToastUtils.show("onRemoteUserEnterRoom: " + userId + ", isOnline" + isOnline);
            }

            @Override
            public void onRemoteUserAudioStream(AlivcLivePusher pusher, String userId, boolean isPushing) {
                ToastUtils.show("onRemoteUserAudioStream: " + userId + ", isPushing: " + isPushing);
            }

            @Override
            public void onRemoteUserVideoStream(AlivcLivePusher pusher, String userId, AlivcLivePlayVideoStreamType videoStreamType, boolean isPushing) {
                ToastUtils.show("onRemoteUserVideoStream: " + userId + ", videoStreamType: " + videoStreamType + ", isPushing: " + isPushing);
            }

            @Override
            public void onAudioPublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
                super.onAudioPublishStateChanged(oldState, newState);
                ToastUtils.show("onAudioPublishStateChanged: " + oldState + "->" + newState);
            }

            @Override
            public void onVideoPublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
                super.onVideoPublishStateChanged(oldState, newState);
                ToastUtils.show("onVideoPublishStateChanged: " + oldState + "->" + newState);
            }

            @Override
            public void onScreenSharePublishStateChanged(AlivcLivePublishState oldState, AlivcLivePublishState newState) {
                super.onScreenSharePublishStateChanged(oldState, newState);
                ToastUtils.show("onScreenSharePublishStateChanged: " + oldState + "->" + newState);
            }

            @Override
            public void onLocalDualAudioStreamPushState(AlivcLivePusher pusher, boolean isPushing) {
                super.onLocalDualAudioStreamPushState(pusher, isPushing);
                ToastUtils.show("onLocalDualAudioStreamPushState: " + isPushing);
            }
        });

        // 设置网络通知事件
        mAlivcLivePusher.setLivePushNetworkListener(new AlivcLivePushNetworkListener() {
            @Override
            public void onNetworkPoor(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onNetworkPoor: ");
            }

            @Override
            public void onNetworkRecovery(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onNetworkRecovery: ");
            }

            @Override
            public void onReconnectStart(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onReconnectStart: ");
            }

            @Override
            public void onConnectionLost(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onConnectionLost: ");
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onConnectionLost();
                }
            }

            @Override
            public void onReconnectFail(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onReconnectFail: ");
            }

            @Override
            public void onReconnectSucceed(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onReconnectSucceed: ");
            }

            @Override
            public void onSendDataTimeout(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onSendDataTimeout: ");
            }

            @Override
            public void onConnectFail(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onConnectFail: ");
            }

            @Override
            public void onNetworkQualityChanged(AlivcLiveNetworkQuality upQuality, AlivcLiveNetworkQuality downQuality) {

            }

            @Override
            public String onPushURLAuthenticationOverdue(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPushURLAuthenticationOverdue: ");
                return null;
            }

            @Override
            public void onPushURLTokenWillExpire(AlivcLivePusher pusher) {
                Log.d(TAG, "onPushURLTokenWillExpire: ");
            }

            @Override
            public void onPushURLTokenExpired(AlivcLivePusher pusher) {
                Log.d(TAG, "onPushURLTokenExpired: ");
            }

            @Override
            public void onSendMessage(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onSendMessage: ");
            }

            @Override
            public void onPacketsLost(AlivcLivePusher alivcLivePusher) {
                Log.d(TAG, "onPacketsLost: ");
            }
        });

        // 【美颜】注册视频前处理回调
        mAlivcLivePusher.setCustomFilter(new AlivcLivePushCustomFilter() {
            @Override
            public void customFilterCreate() {
                initBeautyManager();
            }

            @Override
            public int customFilterProcess(int inputTexture, int textureWidth, int textureHeight, long extra) {
                if (mBeautyManager == null) {
                    return inputTexture;
                }

                return mBeautyManager.onTextureInput(inputTexture, textureWidth, textureHeight);
            }

            @Override
            public void customFilterDestroy() {
                destroyBeautyManager();
            }
        });

        // 记录当前摄像头状态
        mCameraId = mAlivcLivePushConfig.getCameraType();
    }

    private void destroyLivePusher() {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.destroy();
        }
    }

    public boolean isPushing() {
        return mAlivcLivePusher != null && mAlivcLivePusher.isPushing();
    }

    public boolean isPulling(InteractiveUserData userData) {
        if (userData == null) {
            return false;
        }
        InteractiveLivePlayer interactiveLivePlayer = mInteractiveLivePlayerMap.get(userData.getKey());
        if (interactiveLivePlayer != null) {
            return interactiveLivePlayer.isPulling();
        }
        return false;
    }

    private void initWatermarks() {
        // 互动模式下使用该接口添加水印，最多添加一个，多则替代
        mTimestampWatermarkManager = new TimestampWatermarkManager();
        mTimestampWatermarkManager.init(new TimestampWatermarkManager.OnWatermarkListener() {
            @Override
            public void onWatermarkUpdate(Bitmap bitmap) {
                if (mAlivcLivePusher != null) {
                    mAlivcLivePusher.addWaterMark(bitmap, 0, 0, 1);
                }
            }
        });
    }

    private void destroyWatermarks() {
        if (mTimestampWatermarkManager != null) {
            mTimestampWatermarkManager.destroy();
            mTimestampWatermarkManager = null;
        }
    }

    public void setInteractLivePushPullListener(InteractLivePushPullListener listener) {
        mInteractLivePushPullListener = listener;
    }

    private boolean createAlivcLivePlayer(InteractiveUserData userData) {
        if (userData == null) {
            return false;
        }

        String userDataKey = userData.getKey();
        if (mInteractiveLivePlayerMap.containsKey(userDataKey)) {
            Log.i(TAG, "createAlivcLivePlayer already created " + userDataKey);
            return false;
        }

        Log.d(TAG, "createAlivcLivePlayer: " + userDataKey);
        InteractiveLivePlayer alivcLivePlayer = new InteractiveLivePlayer(mContext, AlivcLiveMode.AlivcLiveInteractiveMode);
        alivcLivePlayer.setPullUserData(userData);
        alivcLivePlayer.setMultiInteractPlayInfoListener(new InteractLivePushPullListener() {
            @Override
            public void onPullSuccess(InteractiveUserData userData) {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPullSuccess(userData);
                }
            }

            @Override
            public void onPullError(InteractiveUserData userData, AlivcLivePlayError errorType, String errorMsg) {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPullError(userData, errorType, errorMsg);
                }
            }

            @Override
            public void onPullStop(InteractiveUserData userData) {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPullStop(userData);
                }
            }

            @Override
            public void onPushSuccess() {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushSuccess();
                }
            }

            @Override
            public void onPushError() {
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onPushError();
                }
            }

            @Override
            public void onReceiveSEIMessage(int payload, byte[] data) {
                super.onReceiveSEIMessage(payload, data);
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onReceiveSEIMessage(payload, data);
                }
                String sei = new String(data, StandardCharsets.UTF_8);
                mSEIDelayManager.receiveSEI(SEISourceType.RTC, sei);
            }

            @Override
            public void onVideoEnabled(boolean enable) {
                super.onVideoEnabled(enable);
                if (mInteractLivePushPullListener != null) {
                    mInteractLivePushPullListener.onVideoEnabled(enable);
                }
            }
        });
        mInteractiveLivePlayerMap.put(userDataKey, alivcLivePlayer);
        mInteractiveUserDataMap.put(userDataKey, userData);
        return true;
    }

    public void startPreviewAndPush(InteractiveUserData userData, FrameLayout frameLayout, boolean isAnchor) {
        if (userData == null) {
            Log.e(TAG, "startPreviewAndPush, userData invalid!");
            return;
        }

        mPushUserData = userData;
        mAlivcLivePusher.startPreview(mContext, frameLayout, isAnchor);

        String pushUrl = userData.url;
        if (TextUtils.isEmpty(pushUrl) && InteractiveMode.isInteractive(mInteractiveMode)) {
            pushUrl = AliLiveStreamURLUtil.generateInteractivePushUrl(userData.channelId, userData.userId);
        }

        if (TextUtils.isEmpty(pushUrl)) {
            Log.e(TAG, "startPreviewAndPush, pushUrl invalid!");
            return;
        }

        Log.d(TAG, "startPreviewAndPush: " + isAnchor + ", " + pushUrl);
        mAlivcLivePusher.startPushAsync(pushUrl);

        mSEIDelayManager.registerProvider(userData.userId, new ISEIDelayEventListener() {
            @Override
            public void onEvent(String src, String type, String msg) {
                sendCustomMessage(msg, 5);
            }
        });
    }

    public void stopPreview() {
        mAlivcLivePusher.stopPreview();
    }

    public void updatePreview(FrameLayout frameLayout, boolean isFullScreen) {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.updatePreview(mContext, frameLayout, isFullScreen);
        }
    }

    public void stopPush() {
        if (isPushing()) {
            mAlivcLivePusher.stopPush();
        }
    }

    public void stopCamera() {
        mAlivcLivePusher.stopCamera();
    }

    public void setPullView(InteractiveUserData userData, FrameLayout frameLayout, boolean isAnchor) {
        if (userData == null) {
            return;
        }

        this.mAudienceFrameLayout = frameLayout;

        boolean isNew = createAlivcLivePlayer(userData);

        AlivcLivePlayer livePlayer = mInteractiveLivePlayerMap.get(userData.getKey());
        if (livePlayer == null) {
            Log.e(TAG, "setPullView error: livePlayer is empty");
            return;
        }

        if (isNew) {
            AlivcLivePlayConfig config = new AlivcLivePlayConfig();
            config.isFullScreen = isAnchor;
            config.videoStreamType = userData.videoStreamType;
            config.audioStreamType = userData.audioStreamType;
            livePlayer.setupWithConfig(config);
        }

        livePlayer.setPlayView(frameLayout);
        Log.i(TAG, "setPullView: " + userData);
    }

    public void startPullRTCStream(InteractiveUserData userData) {
        if (userData == null || TextUtils.isEmpty(userData.url)) {
            Log.e(TAG, "startPull error: url is empty");
            return;
        }

        createAlivcLivePlayer(userData);

        AlivcLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(userData.getKey());
        Log.d(TAG, "startPullRTCStream: " + userData.getKey() + ", " + userData.url + ", " + alivcLivePlayer);
        if (alivcLivePlayer != null) {
            alivcLivePlayer.startPlay(userData.url);
        }
    }

    public void stopPullRTCStream(InteractiveUserData userData) {
        if (userData == null) {
            return;
        }
        String userKey = userData.getKey();
        AlivcLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(userKey);
        Log.d(TAG, "stopPullRTCStream: " + userData.getKey() + ", " + userData.url + ", " + alivcLivePlayer);
        if (alivcLivePlayer != null) {
            alivcLivePlayer.stopPlay();
            alivcLivePlayer.destroy();
        }
        mInteractiveLivePlayerMap.remove(userKey);
        mInteractiveUserDataMap.remove(userKey);
    }

    // 暂停推流
    public void pausePush() {
        if (isPushing()) {
            mAlivcLivePusher.pause();
        }
    }

    // 恢复推流
    public void resumePush() {
        if (isPushing()) {
            mAlivcLivePusher.resume();
        }
    }

    // 停止播放RTC流
    public void pausePlayRTCStream(InteractiveUserData userData) {
        Log.d(TAG, "pausePlayRTCStream: " + userData);
        if (userData == null) {
            return;
        }

        String userKey = userData.getKey();
        InteractiveLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(userKey);
        if (alivcLivePlayer == null) {
            return;
        }

        alivcLivePlayer.pauseVideoPlaying();
    }

    // 恢复播放RTC流
    public void resumePlayRTCStream(InteractiveUserData userData) {
        Log.d(TAG, "resumePlayRTCStream: " + userData);
        if (userData == null) {
            return;
        }

        String userKey = userData.getKey();
        InteractiveLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(userKey);
        if (alivcLivePlayer == null) {
            return;
        }

        alivcLivePlayer.resumeVideoPlaying();
    }

    // 停止播放所有RTC流
    public void pausePlayRTCStream() {
        for (InteractiveUserData userData : mInteractiveUserDataMap.values()) {
            pausePlayRTCStream(userData);
        }
    }

    // 恢复播放所有RTC流
    public void resumePlayRTCStream() {
        for (InteractiveUserData userData : mInteractiveUserDataMap.values()) {
            resumePlayRTCStream(userData);
        }
    }

    // 发送自定义消息，发送SEI
    public void sendCustomMessage(String text, int payload) {
        if (LivePushGlobalConfig.IS_DATA_CHANNEL_MESSAGE_ENABLE) {
            mAlivcLivePusher.sendDataChannelMessage(text);
        } else {
            mAlivcLivePusher.sendMessage(text, 0, 0, false, payload);
        }
    }

    // 开始本地录制
    public void startLocalRecord(AlivcLiveLocalRecordConfig recordConfig) {
        boolean flag = false;
        if (mAlivcLivePusher != null) {
            flag = mAlivcLivePusher.startLocalRecord(recordConfig);
        }
        if (!flag) {
            ToastUtils.show("start local record failed!");
        }
    }

    // 结束本地录制
    public void stopLocalRecord() {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.stopLocalRecord();
        }
    }

    public void switchCamera() {
        if (isPushing()) {
            mAlivcLivePusher.switchCamera();
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        }
    }

    public void setMute(boolean isMute) {
        mAlivcLivePusher.setMute(isMute);
    }

    public void enableSpeakerPhone(boolean enableSpeakerPhone) {
        mAlivcLivePusher.enableSpeakerphone(enableSpeakerPhone);
    }

    public void enableAudioCapture(boolean enable) {
        if (enable) {
            mAlivcLivePusher.startAudioCapture(true);
        } else {
            mAlivcLivePusher.stopAudioCapture();
        }
    }

    public void enableLocalCamera(boolean enable) {
        mAlivcLivePusher.enableLocalCamera(enable);
    }

    public void release() {
        mSEIDelayManager.destroy();

        destroyWatermarks();
        clearLiveMixTranscodingConfig();

        stopPush();
        destroyLivePusher();

        mMultiInteractLiveMixStreamsArray.clear();
        mMixInteractLiveTranscodingConfig.setMixStreams(mMultiInteractLiveMixStreamsArray);

        for (AlivcLivePlayer alivcLivePlayer : mInteractiveLivePlayerMap.values()) {
            alivcLivePlayer.stopPlay();
            alivcLivePlayer.destroy();
        }

        mInteractiveLivePlayerMap.clear();
        mInteractiveUserDataMap.clear();

        mInteractLivePushPullListener = null;
    }

    // 单切混的逻辑在各自场景的manager里面，因为PK和连麦的混流布局不太一样。
    // 更新混流、混切单
    public void clearLiveMixTranscodingConfig() {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.setLiveMixTranscodingConfig(null);
        }
    }

    // 遍历当前混流子布局信息
    public AlivcLiveMixStream findMixStreamByUserData(InteractiveUserData userData) {
        if (userData == null || TextUtils.isEmpty(userData.userId)) {
            return null;
        }

        for (AlivcLiveMixStream alivcLiveMixStream : mMultiInteractLiveMixStreamsArray) {
            if (userData.userId.equals(alivcLiveMixStream.getUserId())
                    && alivcLiveMixStream.getMixSourceType() == InteractiveBaseUtil.covertVideoStreamType2MixSourceType(userData.videoStreamType)) {
                return alivcLiveMixStream;
            }
        }

        return null;
    }

    public InteractiveUserData getUserDataByKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        for (String userKey : mInteractiveUserDataMap.keySet()) {
            if (TextUtils.equals(userKey, key)) {
                return mInteractiveUserDataMap.get(userKey);
            }
        }

        return null;
    }

    // 初始化美颜相关资源
    private void initBeautyManager() {
        if (mBeautyManager == null && LivePushGlobalConfig.ENABLE_BEAUTY) {
            // v4.4.4版本-v6.1.0版本，互动模式下的美颜，处理逻辑参考BeautySDKType.INTERACT_QUEEN，即：InteractQueenBeautyImpl；
            // v6.1.0以后的版本（从v6.2.0开始），基础模式下的美颜，和互动模式下的美颜，处理逻辑保持一致，即：QueenBeautyImpl；
            mBeautyManager = BeautyFactory.createBeauty(BeautySDKType.QUEEN, mContext);
            // initialize in texture thread.
            mBeautyManager.init();
            mBeautyManager.setBeautyEnable(LivePushGlobalConfig.ENABLE_BEAUTY);
            mBeautyManager.switchCameraId(mCameraId);
        }
    }

    // 销毁美颜相关资源
    private void destroyBeautyManager() {
        if (mBeautyManager != null) {
            mBeautyManager.release();
            mBeautyManager = null;
        }
    }

    /**
     * ****************** 音频数据回调（参考示例） ****************** [START]
     * <p>
     * 调用建议：1. 调用时机任意；2. 注意清除逻辑
     */
    private static final AlivcLiveAudioSource AUDIO_FRAME_OBSERVER_SOURCE = AlivcLiveAudioSource.PROCESS_CAPTURED;
    private static final AlivcLiveAudioFrameObserverOperationMode AUDIO_FRAME_OBSERVER_MODE = AlivcLiveAudioFrameObserverOperationMode.READ_WRITE;
    private static final AlivcLiveAudioFrameObserverUserDefinedInfoBitMask AUDIO_FRAME_OBSERVER_BIT_MASK = AlivcLiveAudioFrameObserverUserDefinedInfoBitMask.MIX_EXTERNAL_CAPTURE;

    public void enableAudioFrameObserver(boolean enable) {
        if (mAlivcLivePusher == null) {
            return;
        }

        if (enable) {
            AlivcLiveAudioFrameObserverConfig audioFrameObserverConfig = new AlivcLiveAudioFrameObserverConfig();
            audioFrameObserverConfig.mode = AUDIO_FRAME_OBSERVER_MODE;
            audioFrameObserverConfig.userDefinedInfo = AUDIO_FRAME_OBSERVER_BIT_MASK;
            mAlivcLivePusher.enableAudioFrameObserver(true, AUDIO_FRAME_OBSERVER_SOURCE, audioFrameObserverConfig);
            mAlivcLivePusher.setLivePushAudioFrameListener(new AlivcLivePushAudioFrameListener() {
                @Override
                public boolean onCapturedAudioFrame(AlivcLivePushAudioFrame audioFrame) {
                    // 清空 byte buffer，测试是否生效
                    Arrays.fill(audioFrame.data, (byte) 0);
                    return true;
                }

                @Override
                public boolean onProcessCapturedAudioFrame(AlivcLivePushAudioFrame audioFrame) {
                    // 清空 byte buffer，测试是否生效
                    Arrays.fill(audioFrame.data, (byte) 0);
                    return true;
                }

                @Override
                public boolean onPublishAudioFrame(AlivcLivePushAudioFrame audioFrame) {
                    // 清空 byte buffer；注意：设置 AlivcLiveAudioSource.PUB 时，只能读不能写
                    Arrays.fill(audioFrame.data, (byte) 0);
                    return true;
                }

                @Override
                public boolean onPlaybackAudioFrame(AlivcLivePushAudioFrame audioFrame) {
                    // 清空 byte buffer，测试是否生效
                    Arrays.fill(audioFrame.data, (byte) 0);
                    return true;
                }

                @Override
                public boolean onMixedAllAudioFrame(AlivcLivePushAudioFrame audioFrame) {
                    // 清空 byte buffer；注意：设置 AlivcLiveAudioSource.MIXED_ALL 时，只能读不能写
                    Arrays.fill(audioFrame.data, (byte) 0);
                    return true;
                }
            });
        } else {
            mAlivcLivePusher.enableAudioFrameObserver(false, AUDIO_FRAME_OBSERVER_SOURCE, null);
            mAlivcLivePusher.setLivePushAudioFrameListener(null);
        }
    }

    /**
     * [END] ****************** 音频数据回调（参考示例） ******************
     */

    /**
     * ****************** 音效接口（参考示例） ****************** [START]
     */

    // 音效文件 assets 路径
    private static final String SOUND_EFFECT_ASSETS_PATH = "alivc_resource/sound_effect/";

    // 预加载音效
    public void loadAudioEffects() {
        if (mAlivcLivePusher == null || mContext == null) {
            return;
        }
        String soundEffectPath1 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_man_smile.mp3";
        String soundEffectPath2 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_roar.mp3";
        String soundEffectPath3 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_sinister_smile.mp3";
        mAlivcLivePusher.preloadAudioEffect(1, soundEffectPath1);
        mAlivcLivePusher.preloadAudioEffect(2, soundEffectPath2);
        mAlivcLivePusher.preloadAudioEffect(3, soundEffectPath3);
    }

    // 取消预加载音效
    public void unloadAudioEffects() {
        if (mAlivcLivePusher == null || mContext == null) {
            return;
        }
        mAlivcLivePusher.unloadAudioEffect(1);
        mAlivcLivePusher.unloadAudioEffect(2);
        mAlivcLivePusher.unloadAudioEffect(3);
    }

    // 播放音效
    public void playAudioEffects() {
        if (mAlivcLivePusher == null || mContext == null) {
            return;
        }

        String soundEffectPath1 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_man_smile.mp3";
        String soundEffectPath2 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_roar.mp3";
        String soundEffectPath3 = mContext.getFilesDir().getPath() + File.separator + SOUND_EFFECT_ASSETS_PATH + "sound_effect_sinister_smile.mp3";

        AlivcLiveAudioEffectConfig audioEffectConfig1 = new AlivcLiveAudioEffectConfig();
        audioEffectConfig1.needPublish = true;
        audioEffectConfig1.publishVolume = 0;
        audioEffectConfig1.playoutVolume = 0;

        AlivcLiveAudioEffectConfig audioEffectConfig2 = new AlivcLiveAudioEffectConfig();
        audioEffectConfig2.needPublish = false;

        AlivcLiveAudioEffectConfig audioEffectConfig3 = new AlivcLiveAudioEffectConfig();
        audioEffectConfig3.needPublish = true;
        audioEffectConfig3.publishVolume = 100;
        audioEffectConfig3.playoutVolume = 100;
        audioEffectConfig3.loopCycles = 3;

        mAlivcLivePusher.playAudioEffect(1, soundEffectPath1, audioEffectConfig1);
        mAlivcLivePusher.playAudioEffect(2, soundEffectPath2, audioEffectConfig2);
        mAlivcLivePusher.playAudioEffect(3, soundEffectPath3, audioEffectConfig3);
    }

    public void stopAudioEffect() {
        if (mAlivcLivePusher == null) {
            return;
        }
        mAlivcLivePusher.stopAudioEffect(1);
        mAlivcLivePusher.stopAudioEffect(2);
        mAlivcLivePusher.stopAudioEffect(3);
//        mAlivcLivePusher.stopAllAudioEffects();
    }

    public void pauseAudioEffect() {
        if (mAlivcLivePusher == null) {
            return;
        }
        mAlivcLivePusher.pauseAudioEffect(1);
        mAlivcLivePusher.pauseAudioEffect(2);
        mAlivcLivePusher.pauseAudioEffect(3);
//        mAlivcLivePusher.pauseAllAudioEffects();
    }

    public void resumeAudioEffect() {
        if (mAlivcLivePusher == null) {
            return;
        }
        mAlivcLivePusher.resumeAudioEffect(1);
        mAlivcLivePusher.resumeAudioEffect(2);
        mAlivcLivePusher.resumeAudioEffect(3);
//        mAlivcLivePusher.resumeAllAudioEffects();
    }

    /**
     * [END] ****************** 音效接口（参考示例） ******************
     */

    /**
     * ****************** 外部音视频输入/三方推流（参考示例） ****************** [START]
     */
    // 新增外部音频流
    public int addExternalAudioStream(AlivcLivePushExternalAudioStreamConfig externalAudioStreamConfig) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.addExternalAudioStream(externalAudioStreamConfig);
        }
        return -1;
    }

    // 删除外部音频流
    public int removeExternalAudioStream(int streamId) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.removeExternalAudioStream(streamId);
        }
        return -1;
    }

    // 输入外部音频流数据
    public int pushExternalAudioStream(int streamId, AlivcLivePushAudioFrame audioFrame) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.pushExternalAudioStream(streamId, audioFrame);
        }
        return -1;
    }

    // 设置是否与麦克风采集音频混合
    public int setMixedWithMic(boolean mixed) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.setMixedWithMic(mixed);
        }
        return -1;
    }

    // 设置外部音频流播放音量
    public int setExternalAudioStreamPlayoutVolume(int streamId, int playoutVolume) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.setExternalAudioStreamPlayoutVolume(streamId, playoutVolume);
        }
        return -1;
    }

    // 设置外部音频流推流音量
    public int setExternalAudioStreamPublishVolume(int streamId, int publishVolume) {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.setExternalAudioStreamPublishVolume(streamId, publishVolume);
        }
        return -1;
    }

    // 启用外部视频输入源
    public void setExternalVideoSource(boolean enable, boolean useTexture, AlivcLivePlayVideoStreamType videoStreamType, AlivcPreviewDisplayMode previewDisplayMode) {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.setExternalVideoSource(enable, useTexture, videoStreamType, previewDisplayMode);
        }
    }

    // 外部视频输入/三方推流
    public void inputStreamVideoData(byte[] data, int width, int height, int stride, int size, long pts, int rotation) {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.inputStreamVideoData(data, width, height, stride, size, pts, rotation);
        }
    }

    // 外部音频输入/三方推流
    public void inputStreamAudioData(byte[] data, int size, int sampleRate, int channels, long pts) {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.inputStreamAudioData(data, size, sampleRate, channels, pts);
        }
    }

    // 输入外部视频流数据
    public void pushExternalVideoFrame(AlivcLivePusherRawDataSample rawDataSample, AlivcLivePlayVideoStreamType videoStreamType) {
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.pushExternalVideoFrame(rawDataSample, videoStreamType);
        }
    }

    /**
     * [END] ****************** 外部音视频输入/三方推流（参考示例） ******************
     */

    /**
     * ****************** 视频双流（参考示例） ****************** [START]
     */
    // 开始屏幕共享流
    public int startScreenShare() {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.startScreenShare();
        }
        return -1;
    }

    // 停止屏幕共享流
    public int stopScreenShare() {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.stopScreenShare();
        }
        return -1;
    }

    /**
     * [END] ****************** 视频双流（参考示例） ******************
     */

    /**
     * ****************** 音频双流（参考示例） ****************** [START]
     */
    // 开启第二路音频流推送
    public int startDualAudioStream() {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.startLocalDualAudioStream();
        }
        return -1;
    }

    // 停止第二路音频流推送
    public int stopDualAudioStream() {
        if (mAlivcLivePusher != null) {
            return mAlivcLivePusher.stopLocalDualAudioStream();
        }
        return -1;
    }

    /**
     * [END] ****************** 音频双流（参考示例） ******************
     */
}

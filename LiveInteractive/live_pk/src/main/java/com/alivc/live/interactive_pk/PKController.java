package com.alivc.live.interactive_pk;

import static com.alivc.live.interactive_common.utils.LivePushGlobalConfig.mAlivcLivePushConfig;

import android.content.Context;
import android.widget.FrameLayout;

import com.alivc.live.commonbiz.LocalStreamReader;
import com.alivc.live.commonbiz.ResourcesConst;
import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.interactive_common.InteractiveMode;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.InteractLivePushPullListener;
import com.alivc.live.interactive_common.utils.LivePushGlobalConfig;
import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;
import com.alivc.live.pusher.AlivcImageFormat;
import com.alivc.live.pusher.AlivcLiveLocalRecordConfig;
import com.alivc.live.pusher.AlivcLivePushAudioFrame;
import com.alivc.live.pusher.AlivcLivePushExternalAudioStreamConfig;
import com.alivc.live.pusher.AlivcLivePusherRawDataSample;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcResolutionEnum;

import java.io.File;

public class PKController {

    private final PKLiveManager mPKLiveManager;
    private final Context mContext;

    private int mAudioStreamId = -1;

    // 主播信息
    public InteractiveUserData mOwnerUserData;
    // PK主播信息
    public InteractiveUserData mOtherUserData;

    // 外部音视频输入
    private LocalStreamReader mLocalStreamReader;
    private LocalStreamReader mLocalStreamReader2;
    private LocalStreamReader mLocalStreamReader3;

    private boolean mEnableSpeakerPhone = false;

    public PKController(Context context, InteractiveUserData userData) {
        mPKLiveManager = new PKLiveManager();
        mPKLiveManager.init(context.getApplicationContext(), InteractiveMode.PK);

        this.mContext = context;
        initExternalStreamAV();
        mOwnerUserData = userData;
    }

    /**
     * 设置 PK 主播的信息
     */
    public void setPKOtherInfo(InteractiveUserData userData) {
        if (userData == null) {
            return;
        }
        mOtherUserData = userData;
        mOtherUserData.url = AliLiveStreamURLUtil.generateInteractivePullUrl(userData.channelId, userData.userId);
    }

    /**
     * 开始推流
     *
     * @param frameLayout 推流画面渲染 View 的 parent ViewGroup
     */
    public void startPush(FrameLayout frameLayout) {
        externAV();
        mPKLiveManager.startPreviewAndPush(mOwnerUserData, frameLayout, true);
    }

    private void initExternalStreamAV() {
        AlivcResolutionEnum externalStreamResolution = AlivcResolutionEnum.RESOLUTION_720P;
        int externalStreamWidth = AlivcResolutionEnum.getResolutionWidth(externalStreamResolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        int externalStreamHeight = AlivcResolutionEnum.getResolutionHeight(externalStreamResolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        mLocalStreamReader = new LocalStreamReader.Builder()
                .setVideoWith(externalStreamWidth)
                .setVideoHeight(externalStreamHeight)
                .setVideoStride(externalStreamWidth)
                .setVideoSize(externalStreamHeight * externalStreamWidth * 3 / 2)
                .setVideoRotation(0)
                .setAudioSampleRate(44100)
                .setAudioChannel(1)
                .setAudioBufferSize(2048)
                .build();
        mLocalStreamReader2 = new LocalStreamReader.Builder()
                .setVideoWith(externalStreamWidth)
                .setVideoHeight(externalStreamHeight)
                .setVideoStride(externalStreamWidth)
                .setVideoSize(externalStreamHeight * externalStreamWidth * 3 / 2)
                .setVideoRotation(0)
                .setAudioSampleRate(44100)
                .setAudioChannel(1)
                .setAudioBufferSize(2048)
                .build();
        mLocalStreamReader3 = new LocalStreamReader.Builder()
                .setVideoWith(externalStreamWidth)
                .setVideoHeight(externalStreamHeight)
                .setVideoStride(externalStreamWidth)
                .setVideoSize(externalStreamHeight * externalStreamWidth * 3 / 2)
                .setVideoRotation(0)
                .setAudioSampleRate(44100)
                .setAudioChannel(1)
                .setAudioBufferSize(2048)
                .build();
    }

    private void externAV() {
        if (LivePushGlobalConfig.mAlivcLivePushConfig.isExternMainStream()) {
            File yuvFile = ResourcesConst.localCaptureYUVFilePath(mContext);
            mLocalStreamReader.readYUVData(yuvFile, (buffer, pts, videoWidth, videoHeight, videoStride, videoSize, videoRotation) -> {
                mPKLiveManager.inputStreamVideoData(buffer, videoWidth, videoHeight, videoStride, videoSize, pts, videoRotation);
            });
            File pcmFile = ResourcesConst.localCapturePCMFilePath(mContext);
            mLocalStreamReader.readPCMData(pcmFile, (buffer, length, pts, audioSampleRate, audioChannel) -> {
                mPKLiveManager.inputStreamAudioData(buffer, length, audioSampleRate, audioChannel, pts);
            });
        }
    }

    public void startExternalVideoStream() {
        // 启用外部视频输入源
        mAlivcLivePushConfig.setAlivcExternMainImageFormat(AlivcImageFormat.IMAGE_FORMAT_YUVNV12);
        mPKLiveManager.setExternalVideoSource(true, false, AlivcLivePlayVideoStreamType.STREAM_CAMERA,
                AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL);
        File yuvFile = ResourcesConst.localCaptureYUVFilePath(mContext);
        mLocalStreamReader2.readYUVData(yuvFile, (buffer, pts, videoWidth, videoHeight, videoStride, videoSize, videoRotation) -> {
            mPKLiveManager.inputStreamVideoData(buffer, videoWidth, videoHeight, videoStride, videoSize, pts, videoRotation);
        });
    }

    public void stopExternalVideoStream() {
        mPKLiveManager.setExternalVideoSource(false, false, AlivcLivePlayVideoStreamType.STREAM_CAMERA,
                AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL);
        mLocalStreamReader2.stopYUV();
    }

    public void startExternalAudioStream() {
        // 新增外部音频流
        AlivcLivePushExternalAudioStreamConfig externalAudioStreamConfig = new AlivcLivePushExternalAudioStreamConfig();
        externalAudioStreamConfig.channels = 1;
        externalAudioStreamConfig.sampleRate = 44100;
        externalAudioStreamConfig.playoutVolume = 50;
        externalAudioStreamConfig.publishVolume = 50;
        externalAudioStreamConfig.publishStream = 0; // 指定绑定的音频流 1-第二音频流，0-MIC流

        mAudioStreamId = mPKLiveManager.addExternalAudioStream(externalAudioStreamConfig);

        // 设置是否与麦克风采集音频混合
        mPKLiveManager.setMixedWithMic(true);

        // 设置外部音频流播放音量
        mPKLiveManager.setExternalAudioStreamPlayoutVolume(mAudioStreamId, 30);
        // 设置外部音频流推流音量
        mPKLiveManager.setExternalAudioStreamPublishVolume(mAudioStreamId, 100);

        // 输入外部音频流数据
        File pcmFile = ResourcesConst.localCapturePCMFilePath(mContext);
        mLocalStreamReader2.readPCMData(pcmFile, (buffer, length, pts, audioSampleRate, audioChannel) -> {
            AlivcLivePushAudioFrame audioFrame = new AlivcLivePushAudioFrame();
            audioFrame.data = buffer;
            audioFrame.numSamples = length / 8;
            audioFrame.bytesPerSample = 8;
            audioFrame.numChannels = audioChannel;
            audioFrame.samplesPerSec = audioSampleRate;
            mPKLiveManager.pushExternalAudioStream(mAudioStreamId, audioFrame);
        });
    }

    public void stopExternalAudioStream() {
        // 删除外部音频流
        mPKLiveManager.removeExternalAudioStream(mAudioStreamId);
        mLocalStreamReader2.stopPcm();
    }

    // 开启第二路音频流推送
    public void startDualAudioStream() {
        // 开启后第二路音频流推送
        mPKLiveManager.startDualAudioStream();

        // 新增外部音频流，用于第二路音频流推送，注意 publishStream 必须为1
        AlivcLivePushExternalAudioStreamConfig dualAudioStreamConfig = new AlivcLivePushExternalAudioStreamConfig();
        dualAudioStreamConfig.channels = 1;
        dualAudioStreamConfig.sampleRate = 44100;
        dualAudioStreamConfig.playoutVolume = 50;
        dualAudioStreamConfig.publishVolume = 50;
        dualAudioStreamConfig.publishStream = 1; // 指定绑定的音频流 1-第二音频流，0-MIC流

        // 添加音频数据源
        mAudioStreamId = mPKLiveManager.addExternalAudioStream(dualAudioStreamConfig);

        // 输入外部音频流数据
        File pcmFile = ResourcesConst.localCapturePCMFilePath(mContext);
        mLocalStreamReader3.readPCMData(pcmFile, (buffer, length, pts, audioSampleRate, audioChannel) -> {
            AlivcLivePushAudioFrame audioFrame = new AlivcLivePushAudioFrame();
            audioFrame.data = buffer;
            audioFrame.numSamples = length / 8;
            audioFrame.bytesPerSample = 8;
            audioFrame.numChannels = audioChannel;
            audioFrame.samplesPerSec = audioSampleRate;
            // 向第二路音频通道输入音频数据
            mPKLiveManager.pushExternalAudioStream(mAudioStreamId, audioFrame);
        });
    }

    // 停止第二路音频流推送
    public void stopDualAudioStream() {
        // 停止第二路音频流推送
        mPKLiveManager.stopDualAudioStream();

        // 删除外部音频流
        mPKLiveManager.removeExternalAudioStream(mAudioStreamId);

        mLocalStreamReader3.stopPcm();
    }

    // 开始屏幕共享流
    public void startScreenShareStream() {
        mPKLiveManager.startScreenShare();

        mPKLiveManager.setExternalVideoSource(true, false, AlivcLivePlayVideoStreamType.STREAM_SCREEN,
                AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT);

        File yuvFile = ResourcesConst.localCaptureYUVFilePath(mContext);
        mLocalStreamReader3.readYUVData(yuvFile, (buffer, pts, videoWidth, videoHeight, videoStride, videoSize, videoRotation) -> {
            AlivcLivePusherRawDataSample rawDataSample = new AlivcLivePusherRawDataSample();
            rawDataSample.format = AlivcImageFormat.IMAGE_FORMAT_YUVNV12;
            rawDataSample.width = videoWidth;
            rawDataSample.height = videoHeight;
            rawDataSample.frame = buffer;
            rawDataSample.videoFrameLength = videoSize;
            rawDataSample.timestamp = pts;
            mPKLiveManager.pushExternalVideoFrame(rawDataSample, AlivcLivePlayVideoStreamType.STREAM_SCREEN);
        });
    }

    // 停止屏幕共享流
    public void stopScreenShareStream() {
        mPKLiveManager.stopScreenShare();

        mPKLiveManager.setExternalVideoSource(false, false, AlivcLivePlayVideoStreamType.STREAM_SCREEN,
                AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT);

        mLocalStreamReader3.stopYUV();
    }

    /**
     * 开始 PK (拉流)
     */
    public void startPK(InteractiveUserData userData, FrameLayout frameLayout) {
        mPKLiveManager.setPullView(userData, frameLayout, true);
        mPKLiveManager.startPullRTCStream(mOtherUserData);
    }

    /**
     * 停止 PK (拉流)
     */
    public void stopPK() {
        mPKLiveManager.stopPullRTCStream(mOtherUserData);
    }

    public void setPKLiveMixTranscoding(boolean needMix) {
        if (needMix) {
            mPKLiveManager.setLiveMixTranscodingConfig(mOwnerUserData, mOtherUserData, false);
        } else {
            mPKLiveManager.clearLiveMixTranscodingConfig();
        }
    }

    /**
     * 静音对端主播（一般用于PK惩罚环节的业务场景）
     *
     * @param mute 静音
     */
    public void mutePKMixStream(boolean mute) {
        if (!isPKing() || mPKLiveManager == null) {
            return;
        }

        // 是否静音连麦实时流
        if (mute) {
            mPKLiveManager.pauseAudioPlaying(mOtherUserData.getKey());
        } else {
            mPKLiveManager.resumeAudioPlaying(mOtherUserData.getKey());
        }

        // 更新混流静音
        mPKLiveManager.setLiveMixTranscodingConfig(mOwnerUserData, mOtherUserData, mute);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        mPKLiveManager.switchCamera();
    }

    public boolean isPKing() {
        return mPKLiveManager.isPulling(mOtherUserData);
    }

    /**
     * 开始 PK (多人 PK 场景)
     */
    public void startMultiPK(InteractiveUserData userData, FrameLayout frameLayout) {
        if (userData == null) {
            return;
        }
        mPKLiveManager.setPullView(userData, frameLayout, true);
        mPKLiveManager.startPullRTCStream(userData);
    }

    public void setPullView(InteractiveUserData userData, FrameLayout frameLayout) {
        mPKLiveManager.setPullView(userData, frameLayout, true);
    }

    public void updatePreviewView(FrameLayout frameLayout, boolean isFullScreen) {
        mPKLiveManager.updatePreview(frameLayout, isFullScreen);
    }

    /**
     * 停止 PK (多人 PK 场景)
     */
    public void stopMultiPK(InteractiveUserData userData) {
        mPKLiveManager.stopPullRTCStream(userData);
    }

    /**
     * 恢复视频流
     *
     * @param userKey userKey
     */
    public void resumeVideoPlaying(String userKey) {
        mPKLiveManager.resumePlayRTCStream(mPKLiveManager.getUserDataByKey(userKey));
    }

    /**
     * 暂停视频流
     *
     * @param userKey userKey
     */
    public void pauseVideoPlaying(String userKey) {
        mPKLiveManager.pausePlayRTCStream(mPKLiveManager.getUserDataByKey(userKey));
    }

    /**
     * 设置 PK 主播的信息(多人PK场景)
     */
    public void setMultiPKOtherInfo(InteractiveUserData userData) {
        setPKOtherInfo(userData);
    }

    public boolean isPKing(InteractiveUserData userData) {
        return mPKLiveManager.isPulling(userData);
    }

    /**
     * 设置混流 (多人 PK 场景)
     *
     * @param isOwnerUserId 是否是主播自己的 id
     * @param userData      主播 userData (如果 isOwnerUserId 为 true，则 userId 表示主播自己的 id，否则表示 pk 方的主播 id)
     * @param frameLayout   当 isOwnerUserId 为 false 时，需要设置 pk 方主播的混流，需要传递 frameLayout 计算混流位置
     */
    public void addMultiPKLiveMixTranscoding(boolean isOwnerUserId, InteractiveUserData userData, FrameLayout frameLayout) {
        if (isOwnerUserId) {
            mPKLiveManager.addAnchorMixTranscodingConfig(userData);
        } else {
            mPKLiveManager.addAudienceMixTranscodingConfig(userData, frameLayout);
        }
    }

    /**
     * 设置混流 (多人 PK 场景)
     * 当 isOwnerUserId 为 true 时，表示主播自己退出 PK，则删除所有混流设置。否则只删除 PK 方的混流。
     *
     * @param isOwnerUserId 是否是主播自己的 id
     * @param userData      主播 userData (如果 isOwnerUserId 为 true，则 userId 表示主播自己的 id，否则表示 pk 方的主播 id)
     */
    public void removeMultiPKLiveMixTranscoding(boolean isOwnerUserId, InteractiveUserData userData) {
        if (isOwnerUserId) {
            mPKLiveManager.clearLiveMixTranscodingConfig();
        } else {
            mPKLiveManager.removeLiveMixTranscodingConfig(userData);
        }
    }

    public void muteAnchor(String userKey, boolean mute) {
        // 更新连麦静音
        if (mute) {
            mPKLiveManager.pauseAudioPlaying(userKey);
        } else {
            mPKLiveManager.resumeAudioPlaying(userKey);
        }

        // 更新混流静音
        mPKLiveManager.muteAnchorMultiStream(mPKLiveManager.getUserDataByKey(userKey), mute);
    }

    public void setPKLivePushPullListener(InteractLivePushPullListener listener) {
        mPKLiveManager.setInteractLivePushPullListener(listener);
    }

    public void setMultiPKLivePushPullListener(InteractLivePushPullListener listener) {
        mPKLiveManager.setInteractLivePushPullListener(listener);
    }

    public void release() {
        mLocalStreamReader.stopYUV();
        mLocalStreamReader.stopPcm();
        mLocalStreamReader2.stopYUV();
        mLocalStreamReader2.stopPcm();
        mLocalStreamReader3.stopYUV();
        mLocalStreamReader3.stopPcm();
        stopExternalAudioStream();
        stopExternalVideoStream();
        stopDualAudioStream();
        mPKLiveManager.release();
    }

    public void setMute(boolean b) {
        mPKLiveManager.setMute(b);
    }

    public void changeSpeakerPhone() {
        mEnableSpeakerPhone = !mEnableSpeakerPhone;
        mPKLiveManager.enableSpeakerPhone(mEnableSpeakerPhone);
    }

    public void enableAudioCapture(boolean enable) {
        mPKLiveManager.enableAudioCapture(enable);
    }

    public void enableLocalCamera(boolean enable) {
        mPKLiveManager.enableLocalCamera(enable);
    }

    public void sendSEI(String text, int payload) {
        mPKLiveManager.sendCustomMessage(text, payload);
    }

    public void startLocalRecord(AlivcLiveLocalRecordConfig recordConfig) {
        if (mPKLiveManager != null) {
            mPKLiveManager.startLocalRecord(recordConfig);
        }
    }

    public void stopLocalRecord() {
        if (mPKLiveManager != null) {
            mPKLiveManager.stopLocalRecord();
        }
    }
}

package com.alivc.live.interactive_live;

import android.content.Context;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.alivc.live.commonbiz.LocalStreamReader;
import com.alivc.live.commonbiz.ResourcesConst;
import com.alivc.live.commonbiz.test.AliLiveStreamURLUtil;
import com.alivc.live.interactive_common.InteractiveMode;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.interactive_common.listener.InteractLivePushPullListener;
import com.alivc.live.interactive_common.utils.LivePushGlobalConfig;
import com.alivc.live.pusher.AlivcResolutionEnum;

import java.io.File;
import java.util.HashMap;

/**
 * 以主播身份进入连麦互动界面的 Controller
 */
public class AnchorController {

    private final InteractLiveManager mInteractLiveManager;
    private final Context mContext;
    private final LocalStreamReader mLocalStreamReader;

    //主播预览 View
    private FrameLayout mAnchorRenderView;
    //观众连麦预览 View
    private FrameLayout mViewerRenderView;

    // 主播信息
    private InteractiveUserData mAnchorUserData;
    // 连麦观众信息
    private InteractiveUserData mAudienceUserData;

    public AnchorController(Context context, InteractiveUserData userData) {
        mAnchorUserData = userData;

        this.mContext = context;
        AlivcResolutionEnum resolution = LivePushGlobalConfig.mAlivcLivePushConfig.getResolution();
        int width = AlivcResolutionEnum.getResolutionWidth(resolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        int height = AlivcResolutionEnum.getResolutionHeight(resolution, LivePushGlobalConfig.mAlivcLivePushConfig.getLivePushMode());
        mLocalStreamReader = new LocalStreamReader.Builder()
                .setVideoWith(width)
                .setVideoHeight(height)
                .setVideoStride(width)
                .setVideoSize(height * width * 3 / 2)
                .setVideoRotation(0)
                .setAudioSampleRate(44100)
                .setAudioChannel(1)
                .setAudioBufferSize(2048)
                .build();

        // 1v1连麦场景下，如果开启了1080P相机采集，同时设置回调低分辨率texture
        boolean useResolution1080P = LivePushGlobalConfig.mAlivcLivePushConfig.getResolution() == AlivcResolutionEnum.RESOLUTION_1080P;
        if (useResolution1080P) {
            HashMap<String, String> extras = new HashMap<>();
            extras.put("user_specified_observer_texture_low_resolution", "TRUE");
            LivePushGlobalConfig.mAlivcLivePushConfig.setExtras(extras);
        }

        mInteractLiveManager = new InteractLiveManager();
        mInteractLiveManager.init(context, InteractiveMode.INTERACTIVE);

        // 1v1连麦场景下，如果开启了1080P相机采集，同时设置回调低分辨率texture
        if (useResolution1080P) {
            mInteractLiveManager.changeResolution(AlivcResolutionEnum.RESOLUTION_540P);
        }
    }

    /**
     * 设置主播预览 View
     *
     * @param frameLayout 主播预览 View
     */
    public void setAnchorRenderView(FrameLayout frameLayout) {
        this.mAnchorRenderView = frameLayout;
    }

    /**
     * 设置观众预览 View
     *
     * @param frameLayout 观众预览 View
     */
    public void setViewerRenderView(FrameLayout frameLayout) {
        this.mViewerRenderView = frameLayout;
    }

    /**
     * 开始直播
     */
    public void startPush() {
        externAV();
        mInteractLiveManager.startPreviewAndPush(mAnchorUserData, mAnchorRenderView, true);
    }

    private void externAV() {
        if (LivePushGlobalConfig.mAlivcLivePushConfig.isExternMainStream()) {
            File yuvFile = ResourcesConst.localCaptureYUVFilePath(mContext);
            mLocalStreamReader.readYUVData(yuvFile, (buffer, pts, videoWidth, videoHeight, videoStride, videoSize, videoRotation) -> {
                mInteractLiveManager.inputStreamVideoData(buffer, videoWidth, videoHeight, videoStride, videoSize, pts, videoRotation);
            });
            File pcmFile = ResourcesConst.localCapturePCMFilePath(mContext);
            mLocalStreamReader.readPCMData(pcmFile, (buffer, length, pts, audioSampleRate, audioChannel) -> {
                mInteractLiveManager.inputStreamAudioData(buffer, length, audioSampleRate, audioChannel, pts);
            });
        }
    }

    /**
     * 开始连麦
     *
     * @param userData 要连麦的观众信息
     */
    public void startConnect(InteractiveUserData userData) {
        if (userData == null || TextUtils.isEmpty(userData.channelId) || TextUtils.isEmpty(userData.userId)) {
            return;
        }
        mAudienceUserData = userData;
        mInteractLiveManager.setPullView(userData, mViewerRenderView, false);
        mInteractLiveManager.startPullRTCStream(userData);
        mInteractLiveManager.setLiveMixTranscodingConfig(mAnchorUserData, userData);
    }

    /**
     * 结束连麦
     */
    public void stopConnect() {
        mInteractLiveManager.stopPullRTCStream(mAudienceUserData);
        mInteractLiveManager.clearLiveMixTranscodingConfig();
    }

    /**
     * 主播是否正在连麦
     *
     * @return true:正在连麦  false:没有连麦
     */
    public boolean isOnConnected() {
        return mInteractLiveManager.isPulling(mAudienceUserData);
    }

    public void resume() {
        mInteractLiveManager.resumePush();
        mInteractLiveManager.resumePlayRTCStream(mAudienceUserData);
    }

    public void pause() {
        mInteractLiveManager.pausePush();
        mInteractLiveManager.pausePlayRTCStream(mAudienceUserData);
    }

    public void release() {
        mInteractLiveManager.release();
        mLocalStreamReader.stopYUV();
        mLocalStreamReader.stopPcm();
    }

    public void pauseVideoPlaying() {
        mInteractLiveManager.pausePlayRTCStream(mAudienceUserData);
    }

    public void resumeVideoPlaying() {
        mInteractLiveManager.resumePlayRTCStream(mAudienceUserData);
    }

    public void setInteractLivePushPullListener(InteractLivePushPullListener listener) {
        mInteractLiveManager.setInteractLivePushPullListener(listener);
    }

    public void switchCamera() {
        mInteractLiveManager.switchCamera();
    }

    public void enableSpeakerPhone(boolean enable) {
        mInteractLiveManager.enableSpeakerPhone(enable);
    }

    public void setMute(boolean b) {
        mInteractLiveManager.setMute(b);
    }

    public void enableAudioCapture(boolean enable) {
        mInteractLiveManager.enableAudioCapture(enable);
    }

    public void muteLocalCamera(boolean muteLocalCamera) {
        mInteractLiveManager.muteLocalCamera(muteLocalCamera);
    }

    public void enableLocalCamera(boolean enable) {
        mInteractLiveManager.enableLocalCamera(enable);
    }

    public void sendSEI(String text, int payload) {
        mInteractLiveManager.sendCustomMessage(text, payload);
    }
}

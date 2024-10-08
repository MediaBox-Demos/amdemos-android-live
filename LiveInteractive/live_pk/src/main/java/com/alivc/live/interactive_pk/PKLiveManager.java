package com.alivc.live.interactive_pk;

import static com.alivc.live.interactive_common.utils.LivePushGlobalConfig.mAlivcLivePushConfig;

import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;

import com.alivc.live.interactive_common.InteractLiveBaseManager;
import com.alivc.live.interactive_common.InteractiveBaseUtil;
import com.alivc.live.interactive_common.bean.InteractiveUserData;
import com.alivc.live.player.AlivcLivePlayer;
import com.alivc.live.pusher.AlivcLiveMixStream;
import com.alivc.live.pusher.AlivcLiveMixStreamType;
import com.alivc.live.pusher.AlivcLiveTranscodingConfig;

import java.util.ArrayList;

public class PKLiveManager extends InteractLiveBaseManager {

    public void resumeAudioPlaying(String key) {
        AlivcLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(key);
        if (alivcLivePlayer != null) {
            alivcLivePlayer.resumeAudioPlaying();
        }
    }

    public void pauseAudioPlaying(String key) {
        AlivcLivePlayer alivcLivePlayer = mInteractiveLivePlayerMap.get(key);
        if (alivcLivePlayer != null) {
            alivcLivePlayer.pauseAudioPlaying();
        }
    }

    // PK场景下设置混流
    public void setLiveMixTranscodingConfig(InteractiveUserData anchorUserData, InteractiveUserData otherUserData, boolean pkMute) {
        if (anchorUserData == null || TextUtils.isEmpty(anchorUserData.channelId) || TextUtils.isEmpty(anchorUserData.userId)) {
            clearLiveMixTranscodingConfig();
            return;
        }

        if (otherUserData == null || TextUtils.isEmpty(otherUserData.channelId) || TextUtils.isEmpty(otherUserData.userId)) {
            clearLiveMixTranscodingConfig();
            return;
        }

        if (mAlivcLivePushConfig == null) {
            return;
        }

        if (mAlivcLivePusher == null) {
            return;
        }

        ArrayList<AlivcLiveMixStream> mixStreams = new ArrayList<>();

        AlivcLiveMixStream anchorMixStream = new AlivcLiveMixStream();
        anchorMixStream.userId = anchorUserData.userId;
        anchorMixStream.x = 0;
        anchorMixStream.y = 0;
        anchorMixStream.width = mAlivcLivePushConfig.getWidth() / 2;
        anchorMixStream.height = mAlivcLivePushConfig.getHeight() / 2;
        anchorMixStream.zOrder = 1;
        anchorMixStream.backgroundImageUrl = "https://alivc-demo-cms.alicdn.com/versionProduct/resources/pictures/siheng.jpg";

        mixStreams.add(anchorMixStream);

        if (mAudienceFrameLayout != null) {
            AlivcLiveMixStream otherMixStream = new AlivcLiveMixStream();
            otherMixStream.userId = otherUserData.userId;
            otherMixStream.x = mAlivcLivePushConfig.getWidth() / 2;
            otherMixStream.y = 0;
            otherMixStream.width = mAlivcLivePushConfig.getWidth() / 2;
            otherMixStream.height = mAlivcLivePushConfig.getHeight() / 2;
            otherMixStream.zOrder = 2;
            otherMixStream.mixStreamType = pkMute ? AlivcLiveMixStreamType.PURE_VIDEO : AlivcLiveMixStreamType.AUDIO_VIDEO;
            otherMixStream.mixSourceType = InteractiveBaseUtil.covertVideoStreamType2MixSourceType(otherUserData.videoStreamType);
            otherMixStream.backgroundImageUrl = "https://alivc-demo-cms.alicdn.com/versionProduct/resources/pictures/lantu.jpg";

            mixStreams.add(otherMixStream);
        }

        AlivcLiveTranscodingConfig transcodingConfig = new AlivcLiveTranscodingConfig();
        transcodingConfig.mixStreams = mixStreams;
        mAlivcLivePusher.setLiveMixTranscodingConfig(transcodingConfig);
    }

    // 多人PK场景下添加混流
    public void addAnchorMixTranscodingConfig(InteractiveUserData anchorUserData) {
        if (anchorUserData == null || TextUtils.isEmpty(anchorUserData.channelId) || TextUtils.isEmpty(anchorUserData.userId)) {
            return;
        }

        if (mAlivcLivePushConfig == null) {
            return;
        }

        if (mAlivcLivePusher == null) {
            return;
        }

        AlivcLiveMixStream anchorMixStream = new AlivcLiveMixStream();
        anchorMixStream.userId = anchorUserData.userId;
        anchorMixStream.x = 0;
        anchorMixStream.y = 0;
        anchorMixStream.width = mAlivcLivePushConfig.getWidth();
        anchorMixStream.height = mAlivcLivePushConfig.getHeight();
        anchorMixStream.zOrder = 1;
        anchorMixStream.mixSourceType = InteractiveBaseUtil.covertVideoStreamType2MixSourceType(anchorUserData.videoStreamType);
        anchorMixStream.backgroundImageUrl = "https://alivc-demo-cms.alicdn.com/versionProduct/resources/pictures/siheng.jpg";

        mMultiInteractLiveMixStreamsArray.add(anchorMixStream);
        mMixInteractLiveTranscodingConfig.mixStreams = mMultiInteractLiveMixStreamsArray;

        mAlivcLivePusher.setLiveMixTranscodingConfig(mMixInteractLiveTranscodingConfig);
    }

    // 多人连麦混流静音
    public void muteAnchorMultiStream(InteractiveUserData audienceUserData, boolean mute) {
        if (audienceUserData == null || TextUtils.isEmpty(audienceUserData.channelId) || TextUtils.isEmpty(audienceUserData.userId)) {
            return;
        }

        AlivcLiveMixStream mixStream = findMixStreamByUserData(audienceUserData);
        if (mixStream == null) {
            return;
        }

        mixStream.mixStreamType = mute ? AlivcLiveMixStreamType.PURE_VIDEO : AlivcLiveMixStreamType.AUDIO_VIDEO;
        if (mAlivcLivePusher != null) {
            mAlivcLivePusher.setLiveMixTranscodingConfig(mMixInteractLiveTranscodingConfig);
        }
    }

    /**
     * 添加混流配置
     *
     * @param audienceUserData 观众信息
     * @param frameLayout      观众 frameLayout(渲染 View 的 ViewGroup，用于计算混流位置)
     */
    public void addAudienceMixTranscodingConfig(InteractiveUserData audienceUserData, FrameLayout frameLayout) {
        if (audienceUserData == null || TextUtils.isEmpty(audienceUserData.channelId) || TextUtils.isEmpty(audienceUserData.userId)) {
            return;
        }

        if (mAlivcLivePushConfig == null) {
            return;
        }

        if (mAlivcLivePusher == null) {
            return;
        }

        AlivcLiveMixStream audienceMixStream = new AlivcLiveMixStream();
        audienceMixStream.userId = audienceUserData.userId;
        int size = mMultiInteractLiveMixStreamsArray.size() - 1;
        audienceMixStream.x = size % 3 * frameLayout.getWidth() / 3;
        audienceMixStream.y = size / 3 * frameLayout.getHeight() / 3;
        audienceMixStream.width = frameLayout.getWidth() / 3;
        audienceMixStream.height = frameLayout.getHeight() / 3;
        audienceMixStream.zOrder = 2;
        audienceMixStream.mixSourceType = InteractiveBaseUtil.covertVideoStreamType2MixSourceType(audienceUserData.videoStreamType);
        audienceMixStream.backgroundImageUrl = "https://alivc-demo-cms.alicdn.com/versionProduct/resources/pictures/yiliang.png";

        mMultiInteractLiveMixStreamsArray.add(audienceMixStream);
        mMixInteractLiveTranscodingConfig.mixStreams = mMultiInteractLiveMixStreamsArray;

        mAlivcLivePusher.setLiveMixTranscodingConfig(mMixInteractLiveTranscodingConfig);
    }

    // 移除混流
    public void removeLiveMixTranscodingConfig(InteractiveUserData userData) {
        if (userData == null || TextUtils.isEmpty(userData.channelId) || TextUtils.isEmpty(userData.userId)) {
            return;
        }

        AlivcLiveMixStream mixStream = findMixStreamByUserData(userData);
        if (mixStream == null) {
            return;
        }

        mMultiInteractLiveMixStreamsArray.remove(mixStream);

        //多人 PK 混流，结束 PK 后，重新排版混流界面，防止出现覆盖现象
        int size = mMultiInteractLiveMixStreamsArray.size() - 1;
        for (int i = 1; i < mMultiInteractLiveMixStreamsArray.size(); i++) {
            AlivcLiveMixStream alivcLiveMixStream = mMultiInteractLiveMixStreamsArray.get(i);
            alivcLiveMixStream.x = ((size - i) % 3) * alivcLiveMixStream.width;
            alivcLiveMixStream.y = (size - i) / 3 * alivcLiveMixStream.height;
            alivcLiveMixStream.width = alivcLiveMixStream.width;
            alivcLiveMixStream.height = alivcLiveMixStream.height;
        }

        //Array 中只剩主播 id，说明无人连麦
        if (mMultiInteractLiveMixStreamsArray.size() == 1 && mMultiInteractLiveMixStreamsArray.get(0).userId.equals(userData.userId)) {
            clearLiveMixTranscodingConfig();
        } else {
            mMixInteractLiveTranscodingConfig.mixStreams = mMultiInteractLiveMixStreamsArray;
            if (mAlivcLivePusher != null) {
                mAlivcLivePusher.setLiveMixTranscodingConfig(mMixInteractLiveTranscodingConfig);
            }
        }
    }
}

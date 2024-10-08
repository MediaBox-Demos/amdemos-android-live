package com.alivc.live.interactive_common;

import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;
import com.alivc.live.pusher.AlivcLiveMixSourceType;

import java.util.Random;

/**
 * @author keria
 * @date 2023/10/30
 * @brief
 */
public class InteractiveBaseUtil {

    private InteractiveBaseUtil() {
    }

    /**
     * 生成随机房间号和用户id
     *
     * @param sceneType 场景；直播连麦场景随机数0-100，PK场景随机数100-999，以此区分
     * @param listener  回调结果
     */
    public static void generateRandomId(int sceneType, OnRandomIdListener listener) {
        Random random = new Random();
        int randomNumber;

        String channelId;
        String userId;
        if (sceneType == InteractiveConstants.SCENE_TYPE_INTERACTIVE_LIVE) {
            randomNumber = random.nextInt(99) + 1;
            String randomNumberStr = String.valueOf(randomNumber);
            userId = randomNumberStr; // 互动直播的场景，用户ID随机数范围 0-99
            channelId = randomNumberStr; // 互动直播的场景，房间ID随机数范围 0-99
        } else {
            randomNumber = random.nextInt(900) + 100; // 非互动直播的场景，随机数范围 0-999
            String randomNumberStr = String.valueOf(randomNumber);
            channelId = randomNumberStr;// 设置房间ID
            userId = randomNumberStr;// 设置用户ID，与房间ID相同
        }

        if (listener != null) {
            listener.onResult(channelId, userId);
        }
    }

    public static AlivcLiveMixSourceType covertVideoStreamType2MixSourceType(AlivcLivePlayVideoStreamType videoStreamType) {
        if (videoStreamType == AlivcLivePlayVideoStreamType.STREAM_SCREEN) {
            return AlivcLiveMixSourceType.SOURCE_SCREEN;
        }
        return AlivcLiveMixSourceType.SOURCE_CAMERA;
    }

    public interface OnRandomIdListener {
        void onResult(String channelId, String userId);
    }
}

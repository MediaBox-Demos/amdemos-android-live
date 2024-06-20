package com.alivc.live.interactive_common.bean;

import android.text.TextUtils;
import android.util.Log;

import com.alivc.live.player.annotations.AlivcLivePlayAudioStreamType;
import com.alivc.live.player.annotations.AlivcLivePlayVideoStreamType;

import java.io.Serializable;

/**
 * @author keria
 * @date 2023/10/18
 * @note 直播连麦推拉流用户信息
 */
public class InteractiveUserData implements Serializable {

    private static final String TAG = InteractiveUserData.class.getSimpleName();

    public String channelId;
    public String userId;

    public AlivcLivePlayVideoStreamType videoStreamType;
    public AlivcLivePlayAudioStreamType audioStreamType;

    public String url;

    public String getKey() {
        if (!TextUtils.isEmpty(channelId) && !TextUtils.isEmpty(userId)) {
            return String.format("%s_%s_%s", channelId, userId, videoStreamType == AlivcLivePlayVideoStreamType.STREAM_SCREEN ? "screen" : "camera");
        } else if (!TextUtils.isEmpty(url)) {
            return url;
        }
        Log.e(TAG, "invalid interactive user data");
        return null;
    }

    @Override
    public String toString() {
        return getKey();
    }
}

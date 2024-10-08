package com.alivc.live.interactive_common.utils;

import com.alivc.live.annotations.AlivcLiveCameraCaptureOutputPreference;
import com.alivc.live.pusher.AlivcLivePushConfig;

/**
 * 直播连麦公共配置
 *
 * @note 用于传递配置页的一些公共参数到直播连麦页面
 */
public class LivePushGlobalConfig {

    /**
     * 推流配置
     */
    public static AlivcLivePushConfig mAlivcLivePushConfig = new AlivcLivePushConfig();

    /**
     * 多人互动模式/PK
     */
    public static boolean IS_MULTI_INTERACT = false;

    /**
     * 开启 Data Channel 自定义消息通道
     */
    public static boolean IS_DATA_CHANNEL_MESSAGE_ENABLE = false;

    /**
     * 开启强制耳返
     */
    public static boolean IS_EARBACK_OPEN_WITHOUT_HEADSET = false;

    /**
     * H5兼容模式（可与web连麦互通）
     */
    public static boolean IS_H5_COMPATIBLE = false;

    /**
     * 美颜
     */
    public static boolean ENABLE_BEAUTY = true;
    /**
     * 本地日志
     */
    public static boolean ENABLE_LOCAL_LOG = true;

    /**
     * 水印
     */
    public static boolean ENABLE_WATER_MARK = false;

    /**
     * 摄像头采集偏好
     */
    public static AlivcLiveCameraCaptureOutputPreference CAMERA_CAPTURE_OUTPUT_PREFERENCE = AlivcLiveCameraCaptureOutputPreference.PREVIEW;
}

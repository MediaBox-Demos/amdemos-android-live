package com.alivc.live.commonbiz.test;

import android.text.TextUtils;

import java.util.HashMap;

public class AliLiveStreamURLUtil {

    /**
     * 连麦推拉流地址的APPID/APPKEY/PLAY_DOMAIN
     */
    public static final String APP_ID = PushDemoTestConstants.getTestInteractiveAppID();
    public static final String APP_KEY = PushDemoTestConstants.getTestInteractiveAppKey();
    public static final String PLAY_DOMAIN = PushDemoTestConstants.getTestInteractivePlayDomain();

    /**
     * 连麦推拉流地址的RTMP/HTTP-FLV/ARTC协议头
     */
    private static final String RTMP = "rtmp://";
    private static final String HTTP = "http://";
    private static final String FLV = ".flv";
    private static final String ARTC = "artc://";

    /**
     * DOMAIN 固定字段。
     */
    public static final String ALILIVE_INTERACTIVE_DOMAIN = "live.aliyun.com";

    /**
     * 连麦推拉流地址的二级域名（Second-level domain）
     */
    private static final String PULL_SLD = "play";
    private static final String PUSH_SLD = "push";

    /**
     * 连麦推拉流地址的参数配置key
     */
    public static final String BASIC_DOMAIN = "basicDomain";
    public static final String SDK_APP_ID = "sdkAppId";
    public static final String USER_ID = "userId";
    public static final String TIMESTAMP = "timestamp";
    public static final String TOKEN = "token";

    private static final String APP_NAME = "live";

    /**
     * 旁路混流地址用，纯音频--->audio，音视频--->camera
     */
    private static final String STREAM_TASK_TYPE_CAMERA = "camera";
    private static final String STREAM_TASK_TYPE_AUDIO = "audio";

    private AliLiveStreamURLUtil() {
    }

    /**
     * 根据 APPID/APPKEY/PLAY_DOMAIN 连麦配置信息，生成连麦推流地址
     * <p>
     * 需提前配置好以下连麦配置：
     * {@link AliLiveUserSigGenerate#ALILIVE_APPID}
     * {@link AliLiveUserSigGenerate#ALILIVE_APPKEY}
     * {@link AliLiveUserSigGenerate#ALILIVE_PLAY_DOMAIN}
     *
     * @param channelId 频道 ID
     * @param userId    用户 ID
     * @return 连麦推流地址
     */
    public static String generateInteractivePushUrl(String channelId, String userId) {
        return generateInteractiveUrl(channelId, userId, true);
    }

    /**
     * 根据 APPID/APPKEY/PLAY_DOMAIN 连麦配置信息，生成连麦拉流地址
     * <p>
     * 需提前配置好以下连麦配置：
     * {@link AliLiveUserSigGenerate#ALILIVE_APPID}
     * {@link AliLiveUserSigGenerate#ALILIVE_APPKEY}
     * {@link AliLiveUserSigGenerate#ALILIVE_PLAY_DOMAIN}
     *
     * @param channelId 频道 ID
     * @param userId    用户 ID
     * @return 连麦实时拉流地址，RTC用户实时互通用
     */
    public static String generateInteractivePullUrl(String channelId, String userId) {
        return generateInteractiveUrl(channelId, userId, false);
    }

    private static String generateInteractiveUrl(String channelId, String userId, boolean isPush) {
        String sld = isPush ? PUSH_SLD : PULL_SLD;
        long timestamp = AliLiveUserSigGenerate.getTimesTamp();
        String token = AliLiveUserSigGenerate.createToken(APP_ID, APP_KEY, channelId, userId, timestamp);

        return new StringBuilder(ARTC)
                .append(ALILIVE_INTERACTIVE_DOMAIN).append("/")
                .append(sld).append("/")
                .append(channelId)
                .append("?")
                .append(SDK_APP_ID).append("=").append(APP_ID).append("&")
                .append(USER_ID).append("=").append(userId).append("&")
                .append(TIMESTAMP).append("=").append(timestamp).append("&")
                .append(TOKEN).append("=").append(token)
                .toString();
    }

    /**
     * 根据 APPID/APPKEY/PLAY_DOMAIN 连麦配置信息，生成普通观众的CDN拉流地址
     * <p>
     * 需提前配置好以下连麦配置：
     * {@link AliLiveUserSigGenerate#ALILIVE_APPID}
     * {@link AliLiveUserSigGenerate#ALILIVE_APPKEY}
     * {@link AliLiveUserSigGenerate#ALILIVE_PLAY_DOMAIN}
     *
     * @param channelId 频道 ID
     * @param userId    用户 ID
     * @return 旁路直播拉流地址，普通观众用
     */
    public static String generateCDNPullUrl(String channelId, String userId, boolean isAudioOnly) {
        /**
         * 建议将rtmp换成http-flv的形式。理由如下：
         * 在阿里云点播控制台生成地址时，会同时生成RTMP与HTTP-FLV的地址，这两个协议里包含的数据内容是一致的，只是网络协议通道不一样。
         * <p>
         * HTTP协议是互联网主要协议，CDN、运营商、中间网络设备等链路中都对HTTP有很长时间的网络优化，
         * HTTP的默认80/443端口号也是常见白名单端口，不容易被禁用，而RTMP协议比较老，其默认端口号是1935有可能被防火墙等设备禁用，导致异常。
         * 因此在综合网络环境下，HTTP-FLV的稳定性、性能（卡顿、延时）会比RTMP更好。
         */
        return generateCDNFlvPullUrl(channelId, userId, isAudioOnly);
    }

    /**
     * Parse url into auth info
     *
     * @param url url
     * @return url params
     */
    public static HashMap<String, String> parseUrl(String url) {
        HashMap<String, String> map = new HashMap<>();
        if (TextUtils.isEmpty(url)) {
            return map;
        }

        String[] urlParts = url.trim().split("\\?");
        map.put(BASIC_DOMAIN, urlParts[0]);
        if (urlParts.length == 1) {
            return map;
        }

        String[] params = urlParts[1].split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length < 2) {
                continue;
            }
            map.put(keyValue[0], keyValue[1]);
        }

        return map;
    }

    /**
     * Parse stream name from url params
     *
     * @param params url params
     * @return stream name
     */
    public static String parseURLStreamName(HashMap<String, String> params) {
        if (params == null || !params.containsKey(BASIC_DOMAIN)) {
            return "";
        }

        String domain = params.get(BASIC_DOMAIN);
        if (TextUtils.isEmpty(domain)) {
            return "";
        }

        String[] parts = domain.split("/");
        return parts[parts.length - 1];
    }

    private static String generateCDNFlvPullUrl(String channelId, String userId, boolean isAudioOnly) {
        String streamTaskType = isAudioOnly ? STREAM_TASK_TYPE_AUDIO : STREAM_TASK_TYPE_CAMERA;

        return new StringBuilder(HTTP)
                .append(PLAY_DOMAIN).append('/')
                .append(APP_NAME).append('/')
                .append(APP_ID).append('_')
                .append(channelId).append('_')
                .append(userId).append('_')
                .append(streamTaskType)
                .append(FLV)
                .toString();
    }

    private static String generateCDNRtmpPullUrl(String channelId, String userId, boolean isAudioOnly) {
        String streamTaskType = isAudioOnly ? STREAM_TASK_TYPE_AUDIO : STREAM_TASK_TYPE_CAMERA;

        return new StringBuilder(RTMP)
                .append(PLAY_DOMAIN).append('/')
                .append(APP_NAME).append('/')
                .append(APP_ID).append('_')
                .append(channelId).append('_')
                .append(userId).append('_')
                .append(streamTaskType)
                .toString();
    }

}

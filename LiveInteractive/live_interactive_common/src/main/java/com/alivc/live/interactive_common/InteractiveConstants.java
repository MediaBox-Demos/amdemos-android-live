package com.alivc.live.interactive_common;

/**
 * @author keria
 * @date 2024/5/29
 * @brief 互动直播常量类
 */
public @interface InteractiveConstants {
    String DATA_TYPE_INTERACTIVE_USER_DATA = "user_data";
    String DATA_TYPE_IS_ANCHOR = "IS_ANCHOR";
    String DATA_TYPE_SCENE = "SCENE";

    String KEY_TYPE_SCENE_MULTI_PK_16IN = "scene_multi_pk_16in";

    int SCENE_TYPE_INTERACTIVE_LIVE = 0;
    int SCENE_TYPE_PK_LIVE = 1;
}

package com.alivc.live.commonbiz.seidelay.api;

/**
 * @author keria
 * @date 2023/12/5
 * @brief
 */
public interface ISEIDelayEventListener {
    /**
     * SEI delay event callback
     *
     * @param src  SEI delay event source
     * @param type SEI delay event type
     * @param msg  SEI delay event message
     */
    void onEvent(String src, String type, String msg);
}

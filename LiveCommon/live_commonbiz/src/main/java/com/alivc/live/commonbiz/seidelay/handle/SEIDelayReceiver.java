package com.alivc.live.commonbiz.seidelay.handle;

import com.alivc.live.commonbiz.seidelay.SEISourceType;
import com.alivc.live.commonbiz.seidelay.api.ISEIDelayEventListener;
import com.alivc.live.commonbiz.seidelay.api.ISEIDelayHandle;
import com.alivc.live.commonbiz.seidelay.data.SEIDelayProtocol;
import com.alivc.live.commonbiz.seidelay.time.SEIDelayTimeHandler;

import java.util.Map;

/**
 * SEIDelayReceiver handles the reception and processing of SEI data.
 * It notifies listeners with the delay information.
 *
 * @author keria
 * @date 2023/12/5
 * @brief
 */
public class SEIDelayReceiver extends ISEIDelayHandle {

    private static final long INVALID_THRESH_HOLD = 1000 * 60L;

    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * Receives SEI data and processes it to calculate delay and notify listeners.
     *
     * @param sourceType The type of SEI source.
     * @param sei        The SEI data.
     */
    public void receiveSEI(SEISourceType sourceType, String sei) {
        if (listenerHashMap.isEmpty()) {
            return;
        }

        if (!SEIDelayTimeHandler.isNtpTimeUpdated()) {
            return;
        }

        SEIDelayProtocol dataProtocol = new SEIDelayProtocol(sei);
        long ntpTimestamp = SEIDelayTimeHandler.getCurrentTimestamp();
        long delay = ntpTimestamp - dataProtocol.tm;
        if (delay > INVALID_THRESH_HOLD) {
            return;
        }

        notifyListeners(dataProtocol, sourceType, delay);
    }

    /**
     * Notifies all registered listeners with the delay information.
     *
     * @param dataProtocol The SEIDelayProtocol containing SEI data.
     * @param sourceType   The type of SEI source.
     * @param delay        The calculated delay.
     */
    private void notifyListeners(SEIDelayProtocol dataProtocol, SEISourceType sourceType, long delay) {
        for (Map.Entry<String, ISEIDelayEventListener> entry : listenerHashMap.entrySet()) {
            ISEIDelayEventListener eventListener = entry.getValue();
            if (eventListener != null) {
                eventListener.onEvent(dataProtocol.src, sourceType.toString(), String.valueOf(delay));
            }
        }
    }
}
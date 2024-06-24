package com.alivc.live.commonbiz.backdoor;

import com.alivc.live.commonbiz.SharedPreferenceUtils;
import com.alivc.live.commonutils.ContextUtils;

/**
 * @author keria
 * @date 2023/10/13
 * @brief
 */
public class BackDoorInstance {

    private boolean showBareStream;

    private boolean useMultiPK16IN;

    private BackDoorInstance() {
        showBareStream = SharedPreferenceUtils.getBareStream(ContextUtils.getContext());
        useMultiPK16IN = SharedPreferenceUtils.getMultiPK16IN(ContextUtils.getContext());
    }

    public static BackDoorInstance getInstance() {
        return Inner.instance;
    }

    public boolean isShowBareStream() {
        return this.showBareStream;
    }

    public void setShowBareStream(boolean showBareStream) {
        this.showBareStream = showBareStream;
        SharedPreferenceUtils.setBareStream(ContextUtils.getContext(), showBareStream);
    }

    public boolean isUseMultiPK16IN() {
        return this.useMultiPK16IN;
    }

    public void setUseMultiPK16IN(boolean useMultiPK16IN) {
        this.useMultiPK16IN = useMultiPK16IN;
        SharedPreferenceUtils.setMultiPK16IN(ContextUtils.getContext(), useMultiPK16IN);
    }

    private static class Inner {
        private static final BackDoorInstance instance = new BackDoorInstance();
    }
}

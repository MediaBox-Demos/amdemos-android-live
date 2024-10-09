package com.alivc.live.commonutils;

/**
 * 快速点击判断
 *
 * @author ragnar
 * @date 2019-08-14
 */
public class FastClickUtil {
    private static final long PROCESS_MIN_DURATION = 300;
    private static final long PROCESS_MIDDLE_DURATION = 450;
    private static final long PROCESS_LONG_DURATION = 600;
    private static long mLastProcessTime = 0;

    /**
     * 点击间隔时间是否在[PROCESS_MIN_DURATION]ms内
     */
    public static boolean isProcessing() {
        return isProcessing(PROCESS_MIN_DURATION);
    }

    /**
     * 点击间隔时间是否在[PROCESS_MIDDLE_DURATION]ms内
     */
    public static boolean isMiddleProcessing() {
        return isProcessing(PROCESS_MIDDLE_DURATION);
    }

    /**
     * 点击间隔时间是否在[PROCESS_LONG_DURATION]ms内
     */
    public static boolean isLongProcessing() {
        return isProcessing(PROCESS_LONG_DURATION);
    }

    private static boolean isProcessing(long pass) {
        if (mLastProcessTime == 0L) {
            mLastProcessTime = System.currentTimeMillis();
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long passTime = currentTime - mLastProcessTime;
        if (passTime < 0 || passTime > pass) {
            mLastProcessTime = currentTime;
            return false;
        }
        return true;
    }
}

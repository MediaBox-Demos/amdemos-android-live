package com.alivc.live.commonbiz.seidelay.handle;

import com.alivc.live.commonbiz.seidelay.api.ISEIDelayEventListener;
import com.alivc.live.commonbiz.seidelay.api.ISEIDelayHandle;
import com.alivc.live.commonbiz.seidelay.data.SEIDelayProtocol;
import com.alivc.live.commonbiz.seidelay.time.SEIDelayTimeHandler;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles SEIDelay tasks and events.
 * 对象 SEIDelayProvider 是一个处理 SEIDelay 任务和事件的类。
 * 它使用一个计划好的任务执行器来定期运行任务。
 * 当有监听器被添加或移除时，它会相应地启动或停止任务。
 * 这个类主要处理 SEI 延迟相关的逻辑。
 *
 * @author keria
 * @date 2023/12/5
 * @brief
 */
public class SEIDelayProvider extends ISEIDelayHandle {
    private static final long SCHEDULED_EXECUTOR_SERVICE_PERIOD = 2 * 1000L;
    private static final int CORE_POOL_SIZE = 1;

    private ScheduledThreadPoolExecutor mScheduledExecutorService;
    private volatile boolean mTaskRun = false;

    @Override
    public void addListener(String src, ISEIDelayEventListener listener) {
        super.addListener(src, listener);
        if (!mTaskRun && !listenerHashMap.isEmpty()) {
            mTaskRun = true;
            startTask();
        }
    }

    @Override
    public void removeListener(String src) {
        super.removeListener(src);
        if (mTaskRun && listenerHashMap.isEmpty()) {
            mTaskRun = false;
            stopTask();
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        stopTask();
    }

    private void startTask() {
        stopTask();
        mScheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(CORE_POOL_SIZE);
        mScheduledExecutorService.scheduleAtFixedRate(this::provideSEI, 0, SCHEDULED_EXECUTOR_SERVICE_PERIOD, TimeUnit.MILLISECONDS);
    }

    private void stopTask() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            try {
                if (!mScheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    mScheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                mScheduledExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                mScheduledExecutorService = null;
            }
        }
    }

    private void provideSEI() {
        for (Map.Entry<String, ISEIDelayEventListener> entry : listenerHashMap.entrySet()) {
            ISEIDelayEventListener eventListener = entry.getValue();
            if (eventListener != null && SEIDelayTimeHandler.isNtpTimeUpdated()) {
                long ntpTimestamp = SEIDelayTimeHandler.getCurrentTimestamp();
                SEIDelayProtocol dataProtocol = new SEIDelayProtocol(entry.getKey(), ntpTimestamp);
                eventListener.onEvent(dataProtocol.src, dataProtocol.src, dataProtocol.toString());
            }
        }
    }
}

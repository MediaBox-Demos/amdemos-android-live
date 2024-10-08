package com.alivc.live.commonutils.testexecutor;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author baorunchen
 * @date 2019/7/10
 * @brief 通过调度和执行任务来测试 API 在多线程、高并发场景下的可靠性
 * @note 每秒指定次数地切换线程执行任务，并随机在主线程上执行任务
 */
public class TestScheduledExecutor {
    private static final String TAG = "TestScheduledExecutor";

    private static final int TASK_RETRY_COUNT = 3;      // 可配置重试次数
    private static final int THREAD_POOL_SIZE = 8;      // 线程池大小
    private static final long SHUTDOWN_TIMEOUT = 1000;  // 线程池关闭等待时间，毫秒

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ExecutorService[] threadPools = new ExecutorService[THREAD_POOL_SIZE];
    private final AtomicInteger currentPoolIndex = new AtomicInteger(0);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private final int taskInterval;
    private final Strategy strategy;
    private final PriorityBlockingQueue<PriorityTask> priorityQueue = new PriorityBlockingQueue<>();

    public enum Strategy {
        DEFAULT,
        PRIORITY,
        RETRY,
        TIMEOUT,
        DELAY
    }

    /**
     * 公共构造函数，初始化并启动任务调度
     *
     * @param tasks        要执行的任务列表
     * @param taskInterval 执行时间间隔（毫秒）
     * @param strategy     启用的策略
     */
    public TestScheduledExecutor(final List<Runnable> tasks, int taskInterval, Strategy strategy) {
        this.taskInterval = taskInterval;
        this.strategy = strategy;

        // 初始化多个线程池
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            threadPools[i] = Executors.newFixedThreadPool(1, new CustomThreadFactory("ThreadPool-" + (i + 1) + "-Thread-"));
        }

        // 调度任务：按照指定时间间隔执行
        scheduler.scheduleAtFixedRate(() -> {
            int index = currentPoolIndex.getAndIncrement() % THREAD_POOL_SIZE;
            Runnable selectedTask = tasks.get(random.nextInt(tasks.size())); // 随机选择一个任务
            Runnable wrappedTask = wrapWithStrategy(selectedTask);

            RunnableTask task = new RunnableTask(wrappedTask);
            if (strategy == Strategy.PRIORITY) {
                int priority = random.nextInt(100); // 设置任务的优先级
                priorityQueue.add(new PriorityTask(priority, task));
                executePriorityTask();
            } else {
                executeTask(index, task, random.nextBoolean());
            }
        }, 0, taskInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 公共构造函数，初始化并启动任务调度
     *
     * @param tasks        要执行的任务列表
     * @param taskInterval 执行时间间隔（毫秒）
     */
    public TestScheduledExecutor(final List<Runnable> tasks, int taskInterval) {
        this(tasks, taskInterval, Strategy.DEFAULT);
    }

    /**
     * 只接受单个Runnable的构造函数
     *
     * @param runnable     要执行的任务
     * @param taskInterval 执行时间间隔（毫秒）
     */
    public TestScheduledExecutor(Runnable runnable, int taskInterval) {
        this(Collections.singletonList(runnable), taskInterval, Strategy.DEFAULT);
    }

    /**
     * 只接受单个Runnable的构造函数
     *
     * @param runnable     要执行的任务
     * @param taskInterval 执行时间间隔（毫秒）
     * @param strategy     启用的策略
     */
    public TestScheduledExecutor(Runnable runnable, int taskInterval, Strategy strategy) {
        this(Collections.singletonList(runnable), taskInterval, strategy);
    }

    private Runnable wrapWithStrategy(Runnable task) {
        switch (strategy) {
            case DELAY:
                int delay = random.nextInt(taskInterval * 2);
                return new DelayTask(task, delay);
            case RETRY:
                return new RetryTask(task, TASK_RETRY_COUNT);
            case TIMEOUT:
                return new TimeoutTask(task, taskInterval * 2L);
            case PRIORITY:
                // 优先级调度在调度线程处理中
                return task;
            default:
                return task;
        }
    }

    private void executePriorityTask() {
        if (!priorityQueue.isEmpty()) {
            PriorityTask priorityTask = priorityQueue.poll();
            if (priorityTask != null) {
                int index = currentPoolIndex.getAndIncrement() % THREAD_POOL_SIZE;
                executeTask(index, priorityTask, priorityTask.isMainThread);
            }
        }
    }

    private void executeTask(int index, Runnable task, boolean isMainThread) {
        if (isMainThread) {
            mainHandler.post(task);
        } else {
            threadPools[index].execute(task);
        }
    }

    /**
     * 销毁并清理资源
     */
    public void destroy() {
        shutdownExecutor(scheduler, "Scheduler");
        for (ExecutorService threadPool : threadPools) {
            shutdownExecutor(threadPool, "ThreadPool");
        }
    }

    /**
     * 封装的关闭 Executor 服务的方法
     *
     * @param executor 要关闭的 Executor 服务
     * @param name     Executor 服务的名称(用于日志)
     */
    private static void shutdownExecutor(ExecutorService executor, String name) {
        if (executor == null) return;

        executor.shutdown(); // 禁止提交新任务
        try {
            // 首先试图优雅地关闭
            if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow(); // 如果优雅关闭超时，则强制关闭
                if (!executor.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS)) {
                    Log.e(TAG, name + " did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow(); // 响应中断以强制关闭
            Thread.currentThread().interrupt(); // 恢复中断状态
            Log.e(TAG, "Interrupted while shutting down " + name, e);
        }
    }

    /**
     * 自定义线程工厂，用于设置线程的名称和属性
     */
    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    /**
     * 自定义Runnable任务类
     */
    private static class RunnableTask implements Runnable {
        private final Runnable runnable;

        RunnableTask(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            Log.d(TAG, "Executing task in " + Thread.currentThread().getName());
            runnable.run();
        }
    }

    /**
     * 任务延迟策略
     */
    private static class DelayTask implements Runnable {
        private final Runnable task;
        private final int delay;

        public DelayTask(Runnable task, int delay) {
            this.task = task;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "Task interrupted in delay", e);
            }
            task.run();
        }
    }

    /**
     * 任务重试策略
     */
    private static class RetryTask implements Runnable {
        private final Runnable task;
        private final int maxRetries;

        public RetryTask(Runnable task, int maxRetries) {
            this.task = task;
            this.maxRetries = maxRetries;
        }

        @Override
        public void run() {
            int retryCount = 0;
            while (retryCount < maxRetries) {
                try {
                    task.run();
                    break; // 成功执行后退出循环
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= maxRetries) {
                        Log.e(TAG, "Task failed after " + maxRetries + " retries", e);
                    }
                }
            }
        }
    }

    /**
     * 任务超时策略
     */
    private static class TimeoutTask implements Runnable {
        private final Runnable task;
        private final long timeout;

        public TimeoutTask(Runnable task, long timeout) {
            this.task = task;
            this.timeout = timeout;
        }

        @Override
        public void run() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> future = executor.submit(task);
            try {
                future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                Log.e(TAG, "Task timed out", e);
            } catch (Exception e) {
                Log.e(TAG, "Task execution failed", e);
            } finally {
                executor.shutdown();
            }
        }
    }

    /**
     * 任务优先级策略
     */
    private static class PriorityTask implements Runnable, Comparable<PriorityTask> {
        private final int priority;
        private final Runnable task;
        private final boolean isMainThread;

        public PriorityTask(int priority, Runnable task) {
            this(priority, task, false);
        }

        public PriorityTask(int priority, Runnable task, boolean isMainThread) {
            this.priority = priority;
            this.task = task;
            this.isMainThread = isMainThread;
        }

        @Override
        public void run() {
            task.run();
        }

        @Override
        public int compareTo(PriorityTask other) {
            return Integer.compare(other.priority, this.priority); // 高优先级任务优先
        }
    }
}

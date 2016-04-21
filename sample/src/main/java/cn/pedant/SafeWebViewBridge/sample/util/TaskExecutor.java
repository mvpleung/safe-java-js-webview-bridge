/**
 * Summary: 利用线程池重复利用资源，异步执行任务Runnable，减小创建线程开销
 * Version 1.0
 * Date: 13-11-5
 * Time: 上午10:31
 * Copyright: Copyright (c) 2013
 */

package cn.pedant.SafeWebViewBridge.sample.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务调度
 * created on 2016/4/20 10:47
 */
public class TaskExecutor {
    private static ScheduledThreadPoolExecutor gScheduledThreadPoolExecutor = null;
    private static Handler gMainHandler = null;

    //不包含网络传输处理过程的线程池执行对象
    private static AbstractExecutorService gThreadPoolExecutor = null;

    //包含网络传输处理过程的线程池执行对象
    private static AbstractExecutorService gNetProcessThreadPoolExecutor = null;

    // 线程工厂
    private static ThreadFactory threadFactory = new ThreadFactory() {
        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:" + integer.getAndIncrement());
        }
    };

    /**
     * 执行不包含网络传输处理过程的线程
     *
     * @param task
     */
    public static void executeTask(Runnable task) {
        ensureThreadPoolExecutor();
        gThreadPoolExecutor.execute(task);
    }

    /**
     * 执行包含网络传输处理过程的线程，可能存在等待阻塞的状况
     *
     * @param task
     */
    public static void executeNetTask(Runnable task) {
        ensureNetProcessThreadPoolExecutor();
        gNetProcessThreadPoolExecutor.execute(task);
    }

    public static <T> Future<T> submitTask(Callable<T> task) {
        ensureThreadPoolExecutor();
        return gThreadPoolExecutor.submit(task);
    }

    public static void scheduleTask(long delay, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleTaskAtFixedRateIgnoringTaskRunningTime(long initialDelay, long period, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public static void scheduleTaskAtFixedRateIncludingTaskRunningTime(long initialDelay, long period, Runnable task) {
        ensureScheduledThreadPoolExecutor();
        gScheduledThreadPoolExecutor.scheduleWithFixedDelay(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    public static void scheduleTaskOnUiThread(long delay, Runnable task) {
        ensureMainHandler();
        gMainHandler.postDelayed(task, delay);
    }

    public static void runTaskOnUiThread(Runnable task) {
        ensureMainHandler();
        gMainHandler.post(task);
    }

    private static void ensureMainHandler() {
        if (gMainHandler == null) {
            gMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    private static void ensureThreadPoolExecutor() {
        if (gThreadPoolExecutor == null) {
            gThreadPoolExecutor = new ThreadPoolExecutor(5, 10,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(10),
                    threadFactory,
                    new ThreadPoolExecutor.DiscardOldestPolicy());

        }
    }

    private static void ensureNetProcessThreadPoolExecutor() {
        if (gNetProcessThreadPoolExecutor == null) {
            gNetProcessThreadPoolExecutor = new ThreadPoolExecutor(10, 15,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(10),
                    threadFactory,
                    new ThreadPoolExecutor.DiscardOldestPolicy());

        }
    }

    private static void ensureScheduledThreadPoolExecutor() {
        if (gScheduledThreadPoolExecutor == null) {
            gScheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
        }
    }

    public static void shutdown() {
        if (gThreadPoolExecutor != null) {
            gThreadPoolExecutor.shutdown();
            gThreadPoolExecutor = null;
        }

        if (gScheduledThreadPoolExecutor != null) {
            gScheduledThreadPoolExecutor.shutdown();
            gScheduledThreadPoolExecutor = null;
        }
    }
}

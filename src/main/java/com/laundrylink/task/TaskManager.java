package com.laundrylink.task;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** One shared thread pool + one scheduler for the whole app. */
public class TaskManager {

    // Thread pool: at most 4 orders are processed at the same time
    private static final ExecutorService POOL =
            Executors.newFixedThreadPool(4, daemonFactory("laundry-worker"));

    // Scheduler: runs reminder checks repeatedly
    private static final ScheduledExecutorService SCHEDULER =
            Executors.newScheduledThreadPool(1, daemonFactory("laundry-reminder"));

    private TaskManager() {}

    public static <T> Future<T> submit(Callable<T> task) {
        return POOL.submit(task);
    }

    public static void execute(Runnable task) {
        POOL.execute(task);
    }

    public static ScheduledFuture<?> scheduleRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return SCHEDULER.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    public static void shutdown() {
        POOL.shutdown();
        SCHEDULER.shutdownNow();
    }

    // Daemon threads never block the application from closing
    private static ThreadFactory daemonFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
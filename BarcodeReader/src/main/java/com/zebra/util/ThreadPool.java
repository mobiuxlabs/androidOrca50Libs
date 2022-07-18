package com.zebra.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Thread pool tool class, used for asynchronous operations, etc.
 *
 * @author naz
 * Email 961057759@qq.com
 * Date 2020/7/17
 */
public class ThreadPool {
    private static ThreadPoolExecutor executor;

    private ThreadPool() {
    }

    static {
        executor = new ThreadPoolExecutor(
                0,
                50,
                60000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    public static <T> void execute(FutureTask<T> futureTask) {
        executor.execute(futureTask);
    }

    public static <T> T process(Callable<T> task, long timeout) {
        if (task == null) {
            return null;
        }
        Future<T> future = executor.submit(task);
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            if (!future.isCancelled()) {
                future.cancel(true);
            }
        }
        return null;
    }
}

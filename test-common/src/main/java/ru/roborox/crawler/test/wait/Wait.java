package ru.roborox.crawler.test.wait;

import kotlin.KotlinNullPointerException;

import java.util.concurrent.Callable;

import static org.testng.Assert.fail;

public class Wait {
    private Wait() {

    }

    public static <V> V waitFor(Callable<V> callable) {
        return waitFor(5000, callable);
    }

    public static <V> V waitFor(long timeout, Callable<V> callable) {
        long start = System.currentTimeMillis();
        while((System.currentTimeMillis() - start) < timeout) {
            try {
                V value = callable.call();
                if(value != null) {
                    return value;
                }
                Thread.sleep(500);
            } catch (Exception ignored) {
            }
        }
        fail("Failed wait " + callable);
        return null;
    }

    public static void waitAssert(RunnableWithException runnable) throws Exception {
        waitAssert(runnable, 5000);
    }

    public static void waitAssert(RunnableWithException runnable, long timeout) throws Exception {
        final long maxTime = System.currentTimeMillis() + timeout;
        while (true) {
            try {
                runnable.run();
                return;
            } catch (AssertionError | KotlinNullPointerException e) {
                if (System.currentTimeMillis() > maxTime) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {

                }
            }
        }
    }
}

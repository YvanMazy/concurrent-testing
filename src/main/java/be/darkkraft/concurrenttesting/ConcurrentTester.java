package be.darkkraft.concurrenttesting;

import be.darkkraft.concurrenttesting.exception.WrappedThrowable;
import be.darkkraft.concurrenttesting.runnable.ThrowableRunnable;
import be.darkkraft.concurrenttesting.thread.PreparedThread;

import java.util.Objects;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.fail;

public final class ConcurrentTester {

    private ConcurrentTester() throws IllegalAccessException {
        throw new IllegalAccessException("You cannot instantiate a utility class");
    }

    public static void start(final ThrowableRunnable runnable, final int threads) {
        start(runnable, threads, 1, 0L);
    }

    public static void start(final ThrowableRunnable runnable, final int threads, final int iterations) {
        start(runnable, threads, iterations, 0L);
    }

    public static void start(final ThrowableRunnable runnable, final int threads, final long timeout, final TimeUnit unit) {
        start(runnable, threads, 1, timeout, unit);
    }

    public static void start(final ThrowableRunnable runnable, final int threads, final int iterations, final long timeout) {
        start(runnable, threads, iterations, timeout, TimeUnit.MILLISECONDS);
    }

    public static void start(final ThrowableRunnable runnable,
                             final int threads,
                             final int iterations,
                             final long timeout,
                             final TimeUnit unit) {
        Objects.requireNonNull(runnable, "runnable must not be null");
        if (timeout > 0L && unit == null) {
            throw new NullPointerException("unit must not be null");
        }
        final WrappedThrowable throwable = new WrappedThrowable();
        final CyclicBarrier barrier = new CyclicBarrier(threads + 1);

        try {
            // Start all threads
            for (int i = 0; i < threads; i++) {
                final PreparedThread thread = new PreparedThread(barrier, runnable, throwable, iterations);
                thread.start();
            }
            // Waits until all threads are started, this is also used to coordinate the starting of threads
            // One minute is given for the threads to start. This avoids potential deadlocks but should never be exceeded.
            barrier.await(1, TimeUnit.MINUTES);
            // Wait until all threads have finished
            if (timeout <= 0L) {
                barrier.await();
            } else {
                barrier.await(timeout, unit);
            }
            // Fail if there was at least one error
            if (throwable.hasThrowable()) {
                fail(throwable.get());
            }
        } catch (final Exception e) {
            Thread.currentThread().interrupt();
        }
    }

}
package be.yvanmazy.concurrenttesting;

import be.yvanmazy.concurrenttesting.exception.WrappedThrowable;
import be.yvanmazy.concurrenttesting.runnable.BarrierConsumer;
import be.yvanmazy.concurrenttesting.runnable.ThrowableRunnable;

import java.util.Objects;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.fail;

public final class ConcurrentTester {

    public static void run(final BarrierConsumer task, final Consumer<ConcurrentTester> consumer) {
        final ConcurrentTester tester = of(task);
        consumer.accept(tester);
        tester.run();
    }

    public static void run(final ThrowableRunnable task, final Consumer<ConcurrentTester> consumer) {
        final ConcurrentTester tester = of(task);
        consumer.accept(tester);
        tester.run();
    }

    public static ConcurrentTester of(final BarrierConsumer task) {
        return new ConcurrentTester(task);
    }

    public static ConcurrentTester of(final ThrowableRunnable task) {
        return new ConcurrentTester(task);
    }

    private final BarrierConsumer task;
    private BarrierConsumer afterStart;

    private int threads = 1;
    private int iterations = 1;

    private long timeout;
    private TimeUnit timeoutUnit;

    private ConcurrentTester(final BarrierConsumer task) {
        this.task = Objects.requireNonNull(task, "consumer must not be null");
    }

    private ConcurrentTester(final ThrowableRunnable runnable) {
        this(runnable.toBarrierConsumer());
    }

    public void run() {
        if (this.timeout > 0L && this.timeoutUnit == null) {
            throw new NullPointerException("Timeout unit must not be null");
        }
        final WrappedThrowable throwable = new WrappedThrowable();
        final CyclicBarrier barrier = new CyclicBarrier(this.threads + 1);

        try {
            // Start all threads
            for (int i = 0; i < this.threads; i++) {
                final TestWorkerThread thread = new TestWorkerThread(barrier, this.task, throwable, this.iterations);
                thread.start();
            }
            // Waits until all threads are started, this is also used to coordinate the starting of threads
            // One minute is given for the threads to start. This avoids potential deadlocks but should never be exceeded.
            barrier.await(1, TimeUnit.MINUTES);

            // Execute the after-task after all threads are started
            if (this.afterStart != null) {
                this.afterStart.accept(barrier);
            }

            // Wait until all threads have finished
            if (this.timeout <= 0L) {
                barrier.await();
            } else {
                barrier.await(this.timeout, this.timeoutUnit);
            }
            // Fail if there was at least one error
            if (throwable.hasThrowable()) {
                fail(throwable.get());
            }
        } catch (final Exception e) {
            Thread.currentThread().interrupt();
        }
    }

    public ConcurrentTester threads(final int threads) {
        this.threads = threads;
        return this;
    }

    public ConcurrentTester iterations(final int iterations) {
        this.iterations = iterations;
        return this;
    }

    public ConcurrentTester timeout(final long timeout) {
        return this.timeout(timeout, TimeUnit.MILLISECONDS);
    }

    public ConcurrentTester timeout(final long timeout, final TimeUnit unit) {
        this.timeout = timeout;
        this.timeoutUnit = Objects.requireNonNull(unit, "Timeout unit must not be null");
        return this;
    }

    public ConcurrentTester afterStart(final BarrierConsumer afterStart) {
        this.afterStart = afterStart;
        return this;
    }

}
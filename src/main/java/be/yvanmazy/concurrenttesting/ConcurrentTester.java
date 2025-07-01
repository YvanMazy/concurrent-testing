package be.yvanmazy.concurrenttesting;

import be.yvanmazy.concurrenttesting.exception.WrappedThrowable;
import be.yvanmazy.concurrenttesting.runnable.BarrierConsumer;
import be.yvanmazy.concurrenttesting.runnable.ThrowableRunnable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    private int customBarrierParties = -1;

    private long timeout;
    private TimeUnit timeoutUnit;

    private final CountDownLatch runnerLatch = new CountDownLatch(1);
    private final WrappedThrowable globalThrowable = new WrappedThrowable();

    private List<TestWorkerThread> threadList;
    private CyclicBarrier globalBarrier;
    private CyclicBarrier customBarrier;

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

        this.globalBarrier = new CyclicBarrier(this.threads + 1);
        this.customBarrier = new CyclicBarrier(this.customBarrierParties == -1 ? this.threads + 1 : this.customBarrierParties);
        // Create workers threads
        this.threadList = Stream.generate(() -> new TestWorkerThread(this.globalBarrier,
                this.customBarrier,
                this.task,
                this.globalThrowable,
                this.iterations)).limit(this.threads).toList();

        // Start worker threads in another thread to handle properly the timeout
        new Thread(this::startAndAwait, "ConcurrentTester-Runner").start();

        try {
            if (this.timeout > 0L) {
                if (!this.runnerLatch.await(this.timeout, this.timeoutUnit)) {
                    fail("Test timed out");
                }
            } else {
                this.runnerLatch.await();
            }

            // Fail if there was at least one error
            if (this.globalThrowable.hasThrowable()) {
                fail(this.globalThrowable.get());
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final Exception e) {
            fail("Failed to process concurrent test runner", e);
        } finally {
            this.threadList.forEach(TestWorkerThread::interrupt);
        }
    }

    private void startAndAwait() {
        try {
            // Start all threads
            for (final TestWorkerThread thread : this.threadList) {
                thread.start();
            }
            // Waits until all threads are started, this is also used to coordinate the starting of threads
            // One minute is given for the threads to start. This avoids potential deadlocks but should never be exceeded.
            this.globalBarrier.await(1, TimeUnit.MINUTES);

            if (this.globalThrowable.hasThrowable()) {
                return;
            }

            // Execute the after-task after all threads are started
            if (this.afterStart != null) {
                this.afterStart.accept(this.customBarrier);
            }

            if (this.globalThrowable.hasThrowable()) {
                return;
            }

            this.globalBarrier.await();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final Throwable e) {
            this.globalThrowable.provide(e);
        } finally {
            this.runnerLatch.countDown();
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

    public ConcurrentTester customBarrierParties(final int customBarrierParties) {
        this.customBarrierParties = customBarrierParties;
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
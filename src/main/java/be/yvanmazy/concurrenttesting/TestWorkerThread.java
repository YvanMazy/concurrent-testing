package be.yvanmazy.concurrenttesting;

import be.yvanmazy.concurrenttesting.exception.WrappedThrowable;
import be.yvanmazy.concurrenttesting.runnable.BarrierConsumer;

import java.util.Objects;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

final class TestWorkerThread extends Thread {

    private static final AtomicInteger ID = new AtomicInteger(1);

    private final CyclicBarrier barrier;
    private final BarrierConsumer task;
    private final WrappedThrowable throwable;
    private final int iterations;

    TestWorkerThread(final CyclicBarrier barrier, final BarrierConsumer task, final WrappedThrowable throwable, final int iterations) {
        super("ConcurrentTester-Worker-" + ID.incrementAndGet());
        this.barrier = Objects.requireNonNull(barrier, "barrier must not be null");
        this.task = Objects.requireNonNull(task, "task must not be null");
        this.throwable = Objects.requireNonNull(throwable, "throwable must not be null");
        this.iterations = Math.max(iterations, 1);
    }

    @Override
    public void run() {
        try {
            // Wait for all threads for actions to start at the same time.
            // This helps greatly in causing concurrency errors.
            this.barrier.await();
            // Run iterations
            for (int i = 0; i < this.iterations; i++) {
                this.task.accept(this.barrier);
            }
        } catch (final InterruptedException e) {
            this.interrupt();
        } catch (final Throwable e) {
            // Catch any errors that the runnable may throw
            this.throwable.provide(e);
        } finally {
            try {
                this.barrier.await();
            } catch (final Exception e) {
                this.interrupt();
            }
        }
    }

}
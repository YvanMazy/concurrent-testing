package be.darkkraft.concurrenttesting.thread;

import be.darkkraft.concurrenttesting.exception.WrappedThrowable;
import be.darkkraft.concurrenttesting.runnable.ThrowableRunnable;

import java.util.Objects;
import java.util.concurrent.CyclicBarrier;

public final class PreparedThread extends Thread {

    private final CyclicBarrier barrier;
    private final ThrowableRunnable runnable;
    private final WrappedThrowable throwable;
    private final int iterations;

    public PreparedThread(final CyclicBarrier barrier,
                          final ThrowableRunnable runnable,
                          final WrappedThrowable throwable,
                          final int iterations) {
        this.barrier = Objects.requireNonNull(barrier, "barrier must not be null");
        this.runnable = Objects.requireNonNull(runnable, "runnable must not be null");
        this.throwable = Objects.requireNonNull(throwable, "throwable must not be null");
        this.iterations = Math.max(iterations, 1);
    }

    @Override
    public void run() {
        try {
            // Wait for all threads for actions to start at the same time.
            // This helps greatly in causing concurrency errors.
            this.barrier.await();
            for (int j = 0; j < this.iterations; j++) {
                this.runnable.run();
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
package be.yvanmazy.concurrenttesting;

import be.yvanmazy.concurrenttesting.util.AtomicCounter;
import be.yvanmazy.concurrenttesting.util.Counter;
import be.yvanmazy.concurrenttesting.util.NonAtomicCounter;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

// More of a demonstration than a real test
class ConcurrentTesterTest {

    private static final int REPETITION = 10;
    private static final int THREADS = 100;
    private static final int ITERATIONS = 5000;
    private static final int EXPECTED = THREADS * ITERATIONS;

    @Test
    void testNonAtomicCounter() {
        int success = 0;
        for (int i = 0; i < REPETITION; i++) {
            final Counter counter = new NonAtomicCounter();
            ConcurrentTester.run(counter::increment, c -> c.threads(THREADS).iterations(ITERATIONS));
            if (counter.get() == EXPECTED) {
                success++;
            }
        }
        assertNotEquals(REPETITION, success);
    }

    @RepeatedTest(REPETITION)
    void testAtomicCounter() {
        final Counter counter = new AtomicCounter();
        ConcurrentTester.run(counter::increment, c -> c.threads(THREADS).iterations(ITERATIONS));
        assertEquals(EXPECTED, counter.get());
    }

    @RepeatedTest(REPETITION)
    void testRunWithBarrierConsumer() {
        final Counter counter = new AtomicCounter();
        ConcurrentTester.run(barrier -> {
            for (int i = 0; i < ITERATIONS; i++) {
                counter.increment();
            }
            for (int i = 0; i < ITERATIONS; i++) {
                counter.decrement();
            }
            counter.increment();
            barrier.await();
            barrier.await();
            for (int i = 0; i < ITERATIONS; i++) {
                counter.increment();
                counter.decrement();
            }
            counter.decrement();
            barrier.await();
            barrier.await();
            counter.increment();
        }, c -> c.afterStart(barrier -> {
            barrier.await();
            assertEquals(THREADS, counter.get());
            barrier.await();
            barrier.await();
            assertEquals(0, counter.get());
            barrier.await();
        }).threads(THREADS));
        assertEquals(THREADS, counter.get());
    }

    @Test
    @Timeout(value = 5L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testExceptionPropagationOnWorkerThread() {
        assertThrows(AssertionError.class, () -> ConcurrentTester.run(barrier -> {
            throw new RuntimeException("Test");
        }, c -> c.afterStart(CyclicBarrier::await).threads(THREADS)));
    }

    @Test
    @Timeout(value = 15L, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void testTimeoutOnDeadlock() {
        final Lock lock = new ReentrantLock();
        final AssertionFailedError error = assertThrowsExactly(AssertionFailedError.class, () -> {
            ConcurrentTester.run(lock::lock, t -> t.threads(5).timeout(2L, TimeUnit.SECONDS));
        });
        assertEquals("Test timed out", error.getMessage());
    }

}
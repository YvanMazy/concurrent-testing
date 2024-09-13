package be.yvanmazy.concurrenttesting;

import be.yvanmazy.concurrenttesting.util.AtomicCounter;
import be.yvanmazy.concurrenttesting.util.Counter;
import be.yvanmazy.concurrenttesting.util.NonAtomicCounter;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

// More of a demonstration than a real test
class ConcurrentTesterTest {

    private static final int REPETITION = 10;
    private static final int THREADS = 100;
    private static final int ITERATIONS = 1000;
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
            counter.increment();
            barrier.await();
            barrier.await();
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

}
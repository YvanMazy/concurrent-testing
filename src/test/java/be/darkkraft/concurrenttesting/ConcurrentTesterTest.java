package be.darkkraft.concurrenttesting;

import be.darkkraft.concurrenttesting.util.AtomicCounter;
import be.darkkraft.concurrenttesting.util.Counter;
import be.darkkraft.concurrenttesting.util.NonAtomicCounter;
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
        int fail = 0;
        for (int i = 0; i < REPETITION; i++) {
            final Counter counter = new NonAtomicCounter();
            ConcurrentTester.start(counter::increment, THREADS, ITERATIONS);
            if (counter.get() == EXPECTED) {
                fail++;
            }
        }
        assertNotEquals(REPETITION, fail);
    }

    @RepeatedTest(REPETITION)
    void testAtomicCounter() {
        final Counter counter = new AtomicCounter();
        ConcurrentTester.start(counter::increment, THREADS, ITERATIONS);
        assertEquals(EXPECTED, counter.get());
    }

}
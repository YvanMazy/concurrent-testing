package be.yvanmazy.concurrenttesting.util;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicCounter implements Counter {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void increment() {
        this.counter.incrementAndGet();
    }

    @Override
    public int get() {
        return this.counter.get();
    }

}
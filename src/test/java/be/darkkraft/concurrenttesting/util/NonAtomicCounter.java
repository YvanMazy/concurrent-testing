package be.darkkraft.concurrenttesting.util;

public class NonAtomicCounter implements Counter {

    private int counter;

    @Override
    public void increment() {
        this.counter++;
    }

    @Override
    public int get() {
        return this.counter;
    }

}
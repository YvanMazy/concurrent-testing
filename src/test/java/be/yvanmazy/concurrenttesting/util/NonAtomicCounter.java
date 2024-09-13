package be.yvanmazy.concurrenttesting.util;

public class NonAtomicCounter implements Counter {

    private int counter;

    @Override
    public void increment() {
        this.counter++;
    }

    @Override
    public void decrement() {
        this.counter--;
    }

    @Override
    public int get() {
        return this.counter;
    }

}
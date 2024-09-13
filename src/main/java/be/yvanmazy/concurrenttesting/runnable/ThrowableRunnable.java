package be.yvanmazy.concurrenttesting.runnable;

@FunctionalInterface
public interface ThrowableRunnable {

    void run() throws Exception;

    default BarrierConsumer toBarrierConsumer() {
        return barrier -> this.run();
    }

}
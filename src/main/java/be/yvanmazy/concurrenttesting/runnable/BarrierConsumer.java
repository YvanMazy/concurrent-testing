package be.yvanmazy.concurrenttesting.runnable;

import java.util.concurrent.CyclicBarrier;

@FunctionalInterface
public interface BarrierConsumer {

    void accept(final CyclicBarrier barrier) throws Exception;

}
package be.darkkraft.concurrenttesting.exception;

public final class WrappedThrowable {

    private Throwable throwable;

    public WrappedThrowable(final Throwable throwable) {
        this.throwable = throwable;
    }

    public WrappedThrowable() {
    }

    public synchronized void provide(final Throwable throwable) {
        if (this.throwable == null) {
            this.throwable = throwable;
        }
    }

    public synchronized Throwable get() {
        return this.throwable;
    }

    public synchronized boolean hasThrowable() {
        return this.throwable != null;
    }

}
package com.folkol.frx4jl;

/**
 * An Observer is what subscribes to an Observable.
 */
public class Observer<T>
{
    /**
     * <i>onNext</i> will be called when the Observable <i>emits</i> an item.
     */
    public void onNext(T item) {
    }

    /**
     * <i>onComplete</i> may be called by the Observable to notify the Observer
     * that it will emit no more items.
     */
    public void onComplete() {
    }

    /**
     * <i>onError</i> may be called by the Observable to notify the Observer
     * that an error has occurred, and that no more items will be emitted.
     */
    public void onError(Throwable t) {
    }
}

package com.folkol.frx4jl;

import java.util.function.Consumer;

/**
 * An Observable holds on to an <i>onSubscribe</i> callback,
 * which it will invoke whenever an Observer subscribes.
 * <p>
 * The <i>onSubscribe</i> callback may call the Observer's
 * <i>onNext</i> method zero or more time – followed by at most
 * one call to either <i>onComplete</i> or <i>onError</i>.
 * <p>
 * The callbacks may happen from different threads, but they must
 * happen in series – and the Observable must make sure that earlier
 * calls <i>happens before</i> later calls.
 */
public class Observable<T>
{
    private Consumer<Observer<T>> onSubscribe;

    public Observable(Consumer<Observer<T>> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public void subscribe(Observer<T> observer) {
        try {
            onSubscribe.accept(observer);
        } catch(Exception e) {
            observer.onError(e);
        }
    }
}

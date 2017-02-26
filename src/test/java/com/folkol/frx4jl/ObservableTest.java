package com.folkol.frx4jl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ObservableTest
{
    public static final Consumer<Observer<String>> onSubscribe =
        observer -> {
            observer.onNext("Hello");
            observer.onNext("World");
            observer.onNext("!");
            observer.onComplete();
        };

    @Test
    public void testEmit() {
        Observable<String> observable = new Observable<>(onSubscribe);

        List<String> ss = new ArrayList<>();
        observable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                ss.add(item);
            }
        });

        assertEquals(asList("Hello", "World", "!"), ss);
    }

    @Test
    public void testFilter() {
        Observable<String> observable = new Observable<>(onSubscribe);

        Observable<String> filteredObservable =
            new Observable<>(downstream -> observable.subscribe(
                new Observer<String>()
                {
                    @Override
                    public void onNext(String item) {
                        if(item.length() > 1) {
                            downstream.onNext(item);
                        }
                    }
                }));

        List<String> ss = new ArrayList<>();
        filteredObservable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                ss.add(item);
            }
        });

        assertEquals(asList("Hello", "World"), ss);
    }

    @Test
    public void testMap() {
        Observable<String> observable = new Observable<>(onSubscribe);

        Function<String, String> f = String::toUpperCase;
        Observable<String> mappedObservable =
            // Observable::lift(new MappingOperator(f), or Observable::map(f)
            new Observable<>(downstream -> observable.subscribe(
                new Observer<String>()
                {
                    @Override
                    public void onNext(String item) {
                        downstream.onNext(f.apply(item));
                    }
                }));

        List<String> ss = new ArrayList<>();
        mappedObservable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                ss.add(item);
            }
        });

        assertEquals(asList("HELLO", "WORLD", "!"), ss);
    }

    @Test
    public void testMapAndFilter() {
        Observable<String> observable = new Observable<>(onSubscribe);

        Function<String, String> f = String::toUpperCase;
        // Observable::lift(new MappingOperator(f), or Observable::map(f)
        Observable<String> mappedObservable =
            new Observable<>(downstream -> observable.subscribe(
                new Observer<String>()
                {
                    @Override
                    public void onNext(String item) {
                        downstream.onNext(f.apply(item));
                    }
                }));

        Predicate<String> p = str -> str.length() > 1;
        Observable<String> filteredObservable =
            // Observable::lift(new FilterOperator(p), or Observable::filter(p)
            new Observable<>(downstream -> mappedObservable.subscribe(
                new Observer<String>()
                {
                    @Override
                    public void onNext(String item) {
                        if(p.test(item)) {
                            downstream.onNext(item);
                        }
                    }
                }));


        List<String> ss = new ArrayList<>();
        filteredObservable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                ss.add(item);
            }
        });

        assertEquals(asList("HELLO", "WORLD"), ss);
    }

    @Test
    public void testSubscribeOn() throws InterruptedException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        CountDownLatch latchA = new CountDownLatch(1);
        CountDownLatch latchB = new CountDownLatch(1);
        Observable<String> observable = new Observable<>(onSubscribe);

        Observable<String> otherThreadObservable =
            new Observable<>(downstream -> es.submit(() -> {
                try {
                    latchA.await();
                } catch(InterruptedException e) {
                }
                observable.subscribe(
                    new Observer<String>()
                    {
                        @Override
                        public void onNext(String item) {
                            downstream.onNext(item);
                        }

                        @Override
                        public void onComplete() {
                            downstream.onComplete();
                        }
                    }
                );
            }));

        List<String> ss = new ArrayList<>();
        otherThreadObservable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                ss.add(item);
            }

            @Override
            public void onComplete() {
                latchB.countDown();
            }
        });
        latchA.countDown();
        latchB.await();

        assertEquals(asList("Hello", "World", "!"), ss);
        es.shutdown();
    }

    @Test
    public void testObserveOn() throws InterruptedException {
        ExecutorService es = Executors.newSingleThreadExecutor();
        CountDownLatch latch = new CountDownLatch(1);
        Observable<String> observable = new Observable<>(onSubscribe);

        Observable<String> observingObservable =
            new Observable<>(downstream -> observable.subscribe(
                new Observer<String>()
                {
                    @Override
                    public void onNext(String item) {
                        es.submit(() -> downstream.onNext(item));
                    }

                    @Override
                    public void onComplete() {
                        es.submit(downstream::onComplete);
                    }
                }
            ));

        List<String> ss = new ArrayList<>();
        Thread thread = Thread.currentThread();
        observingObservable.subscribe(new Observer<String>()
        {
            @Override
            public void onNext(String item) {
                assertNotEquals(thread, Thread.currentThread());
                ss.add(item);
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });
        latch.await();

        assertEquals(asList("Hello", "World", "!"), ss);
        es.shutdown();
    }
}

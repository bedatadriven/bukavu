/*
 * Copyright 2014-2023 BeDataDriven Groep B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activityinfo.bukavu.shared.observable;

import com.google.common.collect.ImmutableList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.shared.GwtIncompatible;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The Observable class that implements the Reactive Pattern.
 * <p>
 * This class provides methods for subscribing to the Observable
 * @param <T>
 *            the type of the items emitted by the Observable
 */
public abstract class Observable<T> {

    private static final Logger LOGGER = Logger.getLogger(Observable.class.getName());

    private boolean connecting = false;

    protected @Nullable T cachedValue;

    private final List<org.activityinfo.bukavu.shared.observable.Observer<T>> observers = new ArrayList<>();

    /**
     * @return true if the value is being loaded across the network, is being calculated, or
     * is otherwise not yet available.
     */
    @Deprecated
    public boolean isLoading() {
        return cachedValue == null;
    }

    @Deprecated
    public @Nullable T getUnsafe() {
        return cachedValue;
    }

    protected final boolean isConnecting() {
        return connecting;
    }

    protected final @Nullable T getCachedValue() {
        return cachedValue;
    }

    public final Subscription subscribe(final org.activityinfo.bukavu.shared.observable.Observer<T> observer) {

        if(!connecting && observers.isEmpty()) {
            try {
                connecting = true;
                onConnect();
            } finally {
                connecting = false;
            }
        }
        observers.add(observer);

        observer.onChange(cachedValue);

        return new Subscription() {
            @Override
            public void unsubscribe() {
                boolean removed = observers.remove(observer);
                assert removed : "Already unsubscribed!";
                if(observers.isEmpty()) {
                    onDisconnect();
                }
            }
        };
    }

    /**
     * @return true if there any observers subscribed to this Observable.
     */
    public final boolean isConnected() {
        return !observers.isEmpty();
    }

    /**
     * Called when an {@link org.activityinfo.bukavu.shared.observable.Observer} subscribes to notifications.
     *
     *
     */
    protected void onConnect() {
    }

    /**
     * Called when the last {@link org.activityinfo.bukavu.shared.observable.Observer} unsubscribes.
     */
    protected void onDisconnect() {
    }

    /**
     * Notify subscribers that the value has changed by invoking the
     * {@link org.activityinfo.bukavu.shared.observable.Observer#onChange(Observable)}
     * method of all subscribed {@link org.activityinfo.bukavu.shared.observable.Observer}s.
     */
    protected final void fireChange(@Nullable T value) {
        if(cachedValue != value) {
            cachedValue = value;

            // Do not fire while connecting, we will call observer.onChange() ourselves
            // after connection is complete.
            if(!connecting) {
                int s = observers.size();
                if(s == 1) {
                    observers.get(0).onChange(value);
                } else if(s > 1) {
                    List<org.activityinfo.bukavu.shared.observable.Observer<T>> toNotify = new ArrayList<>(observers);
                    for (org.activityinfo.bukavu.shared.observable.Observer<T> observer : toNotify) {
                        observer.onChange(value);
                    }
                }
            }
        }
    }

    /**
     * Transforms this {@code Observable}'s using the given {@code function}
     * @param function a function that is applied to the current any subsequent value of this {@code Observable}
     * @param <R> the type of the result returned by the given {@code function}
     * @return a new {@code Observable}
     */
    public <R> Observable<R> transform(final Function<T, R> function) {
        return transform(SynchronousScheduler.INSTANCE, function);
    }

    /**
     * Transforms this {@code Observable}'s using the given {@code function}
     * @param <R> the type of the result returned by the given {@code function}
     * @param scheduler
     * @param function a function that is applied to the current any subsequent value of this {@code Observable}
     * @return a new {@code Observable}
     */
    public final <R> Observable<R> transform(Scheduler scheduler, final Function<T, R> function) {
        return new ComputedObservable<R>(scheduler, this) {
            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                T argumentValue = (T) arguments[0];
                return function.apply(argumentValue);
            }
        };
    }

    public final <R> Observable<R> transformIf(Function<T, java.util.Optional<R>> function) {
        return join(value -> function.apply(value).map(result -> Observable.just(result)).orElse(loading()));
    }

    public static <T, U, R> Observable<R> transform(Observable<T> t, Observable<U> u, final BiFunction<T, U, R> function) {
        return transform(SynchronousScheduler.INSTANCE, t, u, function);
    }

    public static <T, U, R> Observable<R> transform(Scheduler scheduler, Observable<T> t, Observable<U> u, final BiFunction<T, U, R> function) {
        return new ComputedObservable<R>(scheduler, t, u) {

            @Override
            @SuppressWarnings("unchecked")
            protected R compute(Object[] arguments) {
                T t = (T)arguments[0];
                U u = (U)arguments[1];
                return function.apply(t, u);
            }
        };
    }

    public <R> Observable<R> join(final Function<T, Observable<R>> function) {
        return new ChainedObservable<>(transform(function));
    }

    public <R> Observable<R> join(Scheduler scheduler, final Function<T, Observable<R>> function) {
        return new ChainedObservable<>(transform(scheduler, function));
    }

    public static <X, Y, R> Observable<R> join(Observable<X> x, Observable<Y> y, BiFunction<X, Y, Observable<R>> function) {
        return new ChainedObservable<>(transform(x, y, function));
    }


    public static <X, Y, R> Observable<Optional<R>> mapFlatMap(Observable<Optional<X>> x, Observable<Optional<Y>> y, BiFunction<X, Y, Optional<R>> function) {
        return Observable.transform(x, y, (xx, yy) -> {
            if(xx.isPresent() && yy.isPresent()) {
                return function.apply(xx.get(), yy.get());
            } else {
                return Optional.empty();
            }
        });
    }

    public static <X, Y, R> Observable<R> join(Scheduler scheduler, Observable<X> x, Observable<Y> y, BiFunction<X, Y, Observable<R>> function) {
        return new ChainedObservable<>(transform(scheduler, x, y, function));
    }

    public static <T> Observable<T> just(T value) {
        return new ConstantObservable<>(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<T> loading() {
        return (Observable<T>) Never.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<List<T>> flatten(Scheduler scheduler, List<Observable<T>> list) {
        return new ComputedObservable<List<T>>(scheduler, (List)list) {
            @Override
            protected List<T> compute(Object[] arguments) {
                return (List<T>) ImmutableList.copyOf(arguments);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable<List<T>> flatten(List<Observable<T>> list) {
        return flatten(SynchronousScheduler.INSTANCE, list);
    }

    /**
     *
     * @return a new Observable whose value if is {@code Optional.empty()} if this Observable is loading.
     */
    public Observable<java.util.Optional<T>> toOptional() {
        return new OptionalObservable<T>(this);
    }

    /**
     * Given a collection which is observable, apply the function {@code f} to each of its elements, and join the results
     * in a new list which is itself observable.
     *
     */
    public static <T, TT extends Iterable<T>, R> Observable<List<R>> flatMap(Observable<TT> observableCollection, final Function<T, Observable<R>> f) {
        return observableCollection.join(new Function<TT, Observable<List<R>>>() {
            @Override
            public Observable<List<R>> apply(TT collection) {
                List < Observable < R >> list = new ArrayList<>();
                for (T element : collection) {
                    list.add(f.apply(element));
                }
                return flatten(list);
            }
        });
    }

    /**
     * Given a list of inputs, apply the given {@code function} to each element, then flatten the resulting list
     * of obserables into an observable list.
     */
    public static <T, R> Observable<List<R>> flatJoin(Iterable<T> inputList, Function<T, Observable<R>> function) {
        List<Observable<R>> applied = new ArrayList<>();
        for (T t : inputList) {
            applied.add(function.apply(t));
        }
        return flatten(applied);
    }


    @GwtIncompatible
    public T waitFor() {
        final List<T> collector = new ArrayList<>();
        Subscription subscription = this.subscribe(new org.activityinfo.bukavu.shared.observable.Observer<T>() {
            @Override
            public void onChange(@Nullable T value) {
                if (value != null) {
                    collector.add(value);
                }
            }
        });
        if(collector.isEmpty()) {
            throw new IllegalStateException("Did not load synchronously");
        }
        subscription.unsubscribe();
        return collector.get(0);
    }

    /**
     * Returns a new observable that waits for this observable to finish loading,
     * and then doesn't subsequently change. Useful for loading a value from the server
     * but not tracking changes to it.
     */
    public final Observable<T> sticky() {
        return new Sticky<>(this);
    }

    /**
     * Executes a task that can be divided into batches in the browser to avoid blocking the event loop.
     */
    public static <T> Observable<T> incremental(IncrementalTask<T> task) {
        return new IncrementalObservable<>(task, EventLoopScheduler.SCHEDULER);
    }

    /**
     * Returns a new {@code Observable} that will only fire change notification values
     * when the source's value has actually changed.
     *
     * <p>Note that {@code T} <strong>must</strong> be immutable! If {@code T} includes mutable state,
     * change detection can fail!!!</p>
     *
     */
    public final Observable<T> cacheIfEqual() {
        return new CachedObservable<>(this, Objects::equals);
    }

    public final Observable<T> cacheIfSame() {
        return this;
    }

    public final Observable<T> cache(CachePredicate<T> predicate) {
        return new CachedObservable<>(this, predicate);
    }

    /**
     * Returns a new {@code Observable} value that does not fire any additional loading events after the initial
     * loading event.
     *
     * <p>This is useful to reduce flicker when values are frequently computed but unlikely to change.
     */
    public final Observable<T> optimistic() {
        return new OptimisticObservable<>(this, null);
    }

    /**
     * Returns a new {@code Observable} value that is optimistic to a point. After the given timeout in milliseconds,
     * the observable's value changes to the loading state.
     *
     */
    public final Observable<T> optimistic(int milliseconds) {
        return new OptimisticObservableWithTimeout<>(this, milliseconds, null);
    }


    /**
     * Returns a new {@code Observable} value that is never loading: it will will either take the default value,
     * or the last loaded value.
     */
    public final Observable<T> optimisticWithDefault(T defaultValue) {
        return new OptimisticObservable<>(this, defaultValue);
    }

    public final Observable<T> or(Observable<T> backup) {
        return toOptional().join(primary -> {
            if(primary.isPresent()) {
                return Observable.just(primary.get());
            } else {
                return backup;
            }
        });
    }

    /**
     * Returns a new {@code Observable} value that does not fire any additional loading events after the initial
     * loading event.
     *
     * <p>This is useful to reduce flicker when values are frequently computed but unlikely to change.
     * Unlike {@link #optimistic()}, the new observable wraps its value in {@link MaybeStale} which indicates whether
     * the value is stale.
     */
    public final Observable<MaybeStale<T>> explicitlyOptimistic() {
        return new ExplicitlyOptimisticObservable<>(this);
    }


    public final Observable<T> debounce(int milliseconds) {
        if(!GWT.isClient()) {
            return this;
        } else {
            return new DebouncedObservable<>(this, milliseconds);
        }
    }

    /**
     * Fore each item in the observable {@code keys} collection, uses {@code function} to compute a new observable
     * value. Previous results of {@code function} are cached, so that when a new key is added to the collection,
     * {@code function} is only called for the new keys.
     *
     * @param keys an Observable collection of keys
     * @param function a function which is used to compute an Observable value for each key
     * @param <K> the key type
     * @param <R> the result type
     * @return an Observable map binding keys to their computed, observable values
     */
    public static <K, R> Observable<Map<K, Observable<R>>> computeMap(
            Observable<? extends Collection<K>> keys,
            Function<K, Observable<R>> function) {
        return new ObservableComputedMap<>(keys, function);
    }

}

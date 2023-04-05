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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    @Test
    public void subscriptions() {

        ObservableStub<Integer> observable = new ObservableStub<>();
        assertFalse(observable.isConnected());

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = observable.subscribe(observer);

        assertTrue(observable.isConnected());
        observer.assertChangeFiredOnce();

        observable.updateValue(42);
        observer.assertChangeFiredOnce();
        subscription.unsubscribe();

        // As the last observer disconnects, this observable
        // should transition to disconnected
        assertFalse(observable.isConnected());

        observable.updateValue(43);

        // After disconnection, we should not receive any further
        // notifications
        observer.assertChangeNotFired();
    }

    @Test
    public void transform() {
        ObservableStub<Integer> number = new ObservableStub<>();
        SchedulerStub scheduler = new SchedulerStub();

        Observable<Integer> twice = number.transform(scheduler, new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input * 2;
            }
        });

        MockObserver<Integer> twiceObserver = new MockObserver<>();
        Subscription twiceSubscription = twice.subscribe(twiceObserver);

        twiceObserver.assertChangeFiredOnce();
        assertTrue(twiceObserver.isLoading());

        // When we update the source value, the calculated value should
        // remain in the loading state but enqueue the recomputation
        number.updateValue(42);
        assertTrue(number.isConnected());
        assertTrue(twiceObserver.isLoading());

        // When then the scheduler runs, the value should be recomputed and 
        // the observer notified with the resulting value
        scheduler.runAll();
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twiceObserver.isLoading());
        assertThat(twiceObserver.getLastValue(), equalTo(42 * 2));

        // Setting the input to loading, should immediately trigger a state change to loading in the computed value
        number.setToLoading();
        twiceObserver.assertChangeFiredOnce();
        assertTrue(twiceObserver.isLoading());

        // When the value is changed, we expect the computed value
        // to REMAIN in the loading state, so no change is fired
        number.updateValue(13);
        twiceObserver.assertChangeNotFired();
        assertTrue(twiceObserver.isLoading());

        // ... and when the scheduler runs, the change event
        // is fired upon recalculation
        scheduler.runAll();
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twiceObserver.isLoading());
        assertThat(twiceObserver.getLastValue(), equalTo(13 * 2));

        twiceSubscription.unsubscribe();

        number.updateValue(96);
        twiceObserver.assertChangeNotFired();
    }


    @Test
    public void transformSynchronous() {
        ObservableStub<Integer> number = new ObservableStub<>();

        Observable<Integer> twice = number.transform(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer input) {
                return input * 2;
            }
        });

        MockObserver<Integer> twiceObserver = new MockObserver<>();
        Subscription twiceSubscription = twice.subscribe(twiceObserver);

        twiceObserver.assertChangeFiredOnce();

        assertTrue(twiceObserver.isLoading());

        // When we update the source value, the calculated value should
        // remain in the loading state but enqueue the recomputation
        number.updateValue(42);

        twiceObserver.assertChangeFiredOnce();
        assertFalse(twiceObserver.isLoading());
        assertThat(twiceObserver.getLastValue(), equalTo(42 * 2));

        number.setToLoading();
        twiceObserver.assertChangeFiredOnce();
        assertTrue(twiceObserver.isLoading());

        // When the value is changed, we expect the computed value
        // to REMAIN in the loading state, so no change is fired
        number.updateValue(13);
        twiceObserver.assertChangeFiredOnce();
        assertFalse(twiceObserver.isLoading());
        assertThat(twiceObserver.getLastValue(), equalTo(13 * 2));

        twiceSubscription.unsubscribe();

        number.updateValue(96);
        twiceObserver.assertChangeNotFired();
    }

    @Test
    public void chained() {
        final RemoteServiceStub remoteService = new RemoteServiceStub();
        ObservableStub<Integer> id = new ObservableStub<>();
        Observable<String> name = id.join(new Function<Integer, Observable<String>>() {
            @Override
            public Observable<String> apply(Integer input) {
                return remoteService.queryName(input);
            }
        });

        MockObserver<String> nameObserver = new MockObserver<>();
        Subscription nameSubscription = name.subscribe(nameObserver);
        nameObserver.assertChangeFiredOnce();

        id.updateValue(42);

        // Initially, the value computed on the value from the remote
        // service should also be "loading"
        assertTrue(nameObserver.isLoading());

        // Once the remote service completes, the calculated value should also be updated
        remoteService.completePending();
        nameObserver.assertChangeFiredOnce();
        assertThat(nameObserver.getLastValue(), equalTo("name42"));
    }

    @Test
    public void chainedObservable() {
        Observable<Integer> three = Observable.just(3);
        PendingValue<Observable<Integer>> x = new PendingValue<>();
        Observable<Integer> y = x.join(x_ -> x_);

        MockObserver observer = new MockObserver();
        y.subscribe(observer);

        observer.assertLoading();
        x.updateValue(three);

        observer.assertValueEquals(3);

        x.clear();
        x.updateValue(three);

        observer.assertValueEquals(3);

    }

    @Test
    public void joined() {

        StatefulValue<Integer> x = new StatefulValue<>(0);
        Observable<Integer> abs = x.join(new Function<Integer, Observable<Integer>>() {
            @Override
            public Observable<Integer> apply(Integer integer) {
                return Observable.just(Math.abs(integer));
            }
        });

        Observable<Double> sqrt = abs.join(new Function<Integer, Observable<Double>>() {
            @Override
            public Observable<Double> apply(Integer integer) {
                return Observable.just(Math.sqrt(integer));
            }
        });

        CountingObserver<Double> sqrtObserver = new CountingObserver<>();
        sqrt.subscribe(sqrtObserver);

        x.updateIfNotEqual(-16);

        assertThat(sqrtObserver.countChanges(), equalTo(2));


    }

    @Test
    public void chainedConnection() {
        final ObservableStub<Integer> id = new ObservableStub<>(1);
        final ObservableStub<String> remoteValue1 = new ObservableStub<>();
        final ObservableStub<String> remoteValue2 = new ObservableStub<>();

        Observable<String> result = id.join(new Function<Integer, Observable<String>>() {
            @Override
            public Observable<String> apply(Integer input) {
                if(input == 1) {
                    return remoteValue1;
                } else {
                    return remoteValue2;
                }
            }
        });

        MockObserver<String> resultObserver = new MockObserver<>();
        result.subscribe(resultObserver);
        assertTrue(remoteValue1.isConnected());

        remoteValue1.updateValue("name1");
        assertThat(resultObserver.getLastValue(), equalTo("name1"));
    }


    @Test
    public void reentrantConnections() {
        StatefulValue<Integer> x = new StatefulValue<>(3);
        Observable<Integer> z = x.transform(x_ -> x_ * x_);

        z.subscribe(zo1 -> {
            System.out.println("zo1 change");

            z.subscribe(zo2 -> {
                System.out.println("zo2 change");
            });
        });

    }

    @Test
    public void chainedComputations() {
        Observable<Integer> a = Observable.just(1);
        Observable<Integer> b = Observable.just(2);
        Observable<Integer> x = a.transform(_a -> _a * 10);
        Observable<Integer> y = b.transform(_b -> _b * 10);
        Observable<Integer> z = Observable.transform(x, y, (_x, _y) -> _x + _y);

        z.subscribe(z_ -> {
            assertThat(z_, not(nullValue()));
        });
    }

    private class ExpensiveOperation implements Function<Integer, Integer> {

        int computationCount = 0;

        @Override
        public Integer apply(Integer input) {
            computationCount++;
            return input * 10;
        }
    }

    @Test
    public void cachedTransformation() {

        ExpensiveOperation operation = new ExpensiveOperation();

        StatefulValue<Integer> a = new StatefulValue<>(1);
        Observable<Integer> b = a.cacheIfEqual().transform(operation);

        CountingObserver observer = new CountingObserver();

        // Connect and disconnect three times
        for (int i = 0; i < 3; i++) {
            Subscription subscription = b.subscribe(observer);
            subscription.unsubscribe();
        }

        assertThat(observer.countChanges(), equalTo(3));
        assertThat(operation.computationCount, equalTo(1));

        a.updateValue(2);


        // Connect and disconnect another three times
        for (int i = 0; i < 3; i++) {
            Subscription subscription = b.subscribe(observer);
            subscription.unsubscribe();
        }

        assertThat(operation.computationCount, equalTo(2));
    }

    @Test
    public void cachedEquals() {

        Optional<String> a1 = Optional.of("a");
        Optional<String> a2 = Optional.of("a");

        assertNotSame(a1, a2);

        PendingValue<Optional<String>> state = new PendingValue<>();

        Observable<Optional<String>> cached = state.cacheIfEqual();

        MockObserver<Optional<String>> observer = new MockObserver<>();
        Subscription subscription = cached.subscribe(observer);

        observer.assertChangeFiredOnce();

        state.updateValue(a1);

        observer.assertChangeFiredOnce();
        observer.assertValueEquals(Optional.of("a"));

        state.updateValue(a2);

        observer.assertChangeNotFired();
        observer.assertValueEquals(Optional.of("a"));

        state.updateValue(Optional.of("b"));
        observer.assertChangeFiredOnce();
        observer.assertValueEquals(Optional.of("b"));

        state.updateValue(null);
        observer.assertChangeFiredOnce();
        observer.assertLoading();

        assertTrue(state.isConnected());
        assertTrue(cached.isConnected());

        subscription.unsubscribe();

        assertFalse(state.isConnected());
        assertFalse(cached.isConnected());
    }

    @Test
    public void flattenedCache() {

        PendingValue<String> a = new PendingValue<>();
        PendingValue<String> b = new PendingValue<>();

        List<Observable<String>> ab = new ArrayList<>();
        ab.add(a);
        ab.add(b);

        Observable<List<String>> flattened = Observable.flatten(ab);
        Observable<List<String>> cached = flattened.cacheIfEqual();

        MockObserver<List<String>> observer = new MockObserver<>();
        Subscription subscription = cached.subscribe(observer);

        observer.assertLoading();
        observer.assertChangeFiredOnce();

        a.updateValue("a");
        b.updateValue("b");

        observer.assertChangeFiredOnce();
        observer.assertValueEquals(Arrays.asList("a", "b"));

        b.updateValue("b2");

        observer.assertChangeFiredOnce();
        observer.assertValueEquals(Arrays.asList("a", "b2"));
    }


    @Test
    public void testOr() {

        PendingValue<Integer> remote = new PendingValue<>();
        Observable<Integer> stale = Observable.just(41);

        Observable<Integer> backup = remote.or(stale);

        MockObserver<Integer> observer = new MockObserver<>();
        backup.subscribe(observer);

        assertThat(remote.isConnected(), equalTo(true));
        assertThat(observer.getLastValue(), equalTo(41));

        remote.updateValue(42);

        assertThat(observer.getLastValue(), equalTo(42));

        remote.updateValue(43);
        assertThat(remote.isConnected(), equalTo(true));

        assertThat(observer.getLastValue(), equalTo(43));
    }

    @Test
    public void misbehavingTransform() {
        PendingValue<Integer> value = new PendingValue<>();
        Observable<Integer> result = value.transform(x -> {
            if (x == 1) {
                throw new IllegalArgumentException();
            } else {
                return x * 2;
            }
        });

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subcription = result.subscribe(observer);

        assertTrue(observer.isLoading());

        value.updateValue(2);

        assertThat(observer.getLastValue(), equalTo(4));

        value.updateValue(1);

        assertTrue(observer.isLoading());
    }

    @Test
    public void caching() {
        PendingValue<Integer> input = new PendingValue<>();
        Observable<Integer> result = input.transform(x -> x % 10).cacheIfEqual();

        MockObserver<Integer> observer = new MockObserver<>();
        result.subscribe(observer);

        assertTrue(observer.isLoading());
        observer.assertChangeFiredOnce();

        input.updateValue(2);
        observer.assertChangeFiredOnce();
        assertFalse(observer.isLoading());
        assertThat(observer.getLastValue(), equalTo(2));

        input.updateValue(22);
        observer.assertChangeNotFired();
        assertThat(observer.getLastValue(), equalTo(2));

        input.updateValue(301);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(1));

    }

    @Test
    public void optimistic() {
        PendingValue<Integer> input = new PendingValue<>();
        Observable<Integer> result = input.transform(x -> x * 2).optimistic();

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = result.subscribe(observer);

        assertTrue(observer.isLoading());
        observer.assertChangeFiredOnce();

        input.updateValue(2);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(4));

        // If the input transitions to "loading", then optimistically
        // assume the value does not change.
        input.updateValue(null);
        observer.assertChangeNotFired();
        assertThat(observer.getLastValue(), equalTo(4));

        // When a new value is loaded, THEN fire
        input.updateValue(80);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(160));

        subscription.unsubscribe();
        assertFalse(result.isConnected());
        assertFalse(input.isConnected());

        // JVM allows us to block for result
        assertThat(result.waitFor(), equalTo(160));
    }

    @Test
    public void binaryFunction() {

        PendingValue<Integer> a = new PendingValue<>();
        PendingValue<Integer> b = new PendingValue<>();

        Observable<Integer> result = Observable.transform(a, b, (a_, b_) -> a_ + b_);
        
        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = result.subscribe(observer);

        assertTrue(observer.isLoading());
        observer.assertChangeFiredOnce();

        // If we update the first argument, nothing will change, we still don't have
        // both values...
        a.updateValue(1);
        observer.assertChangeNotFired();

        // Once the second value is available, then we should be ready to compute
        b.updateValue(4);

        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(5));

        subscription.unsubscribe();

        assertFalse(result.isConnected());
        assertFalse(a.isConnected());
        assertFalse(b.isConnected());
    }

    @Test
    public void chainedLoading() {
        PendingValue<Integer> a = new PendingValue<>();
        PendingValue<Integer> b = new PendingValue<>();
        Observable<Integer> chained = a.join(i -> {
            if (i % 2 == 0) {
                return Observable.just(i / 2);
            } else {
                return b.transform(b_ -> b_ * 4);
            }
        });

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = chained.subscribe(observer);

        observer.assertChangeFiredOnce();
        assertTrue(observer.isLoading());

        a.updateValue(4);

        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(2));

        a.updateValue(null);
        observer.assertChangeFiredOnce();
        assertTrue(observer.isLoading());

        // If we change to an odd number, we should not expect a change
        // because b is still loading
        a.updateValue(9);
        observer.assertChangeNotFired();
        assertTrue(observer.isLoading());

        // Now when b comes online...
        b.updateValue(41);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(41 * 4));

        // If a changes to another odd number, we create a new transformed observable,
        // so an event will befired
        a.updateValue(13);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(41 * 4));

        assertTrue(b.isConnected());

        // If a transitions to a even number, we should disconnect from b
        a.updateValue(12);
        observer.assertChangeFiredOnce();
        assertThat(observer.getLastValue(), equalTo(6));

        assertFalse(b.isConnected());

        subscription.unsubscribe();

        assertFalse(a.isConnected());
        assertFalse(b.isConnected());

    }
}

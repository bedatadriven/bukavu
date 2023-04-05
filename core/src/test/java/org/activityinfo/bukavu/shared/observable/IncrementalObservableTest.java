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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncrementalObservableTest {

    private static class Adder implements IncrementalTask<Integer> {
        private final int targetSum;
        private int currentSum;


        private Adder(int targetSum) {
            this.targetSum = targetSum;
        }

        @Override
        public @Nullable Integer execute() {
            currentSum ++;
            return currentSum;
        }

        @Override
        public boolean isDone() {
            return currentSum == targetSum;
        }
    }


    @Test
    public void test() {

        Adder task = new Adder(3);
        SchedulerStub scheduler = new SchedulerStub();
        Observable<Integer> incremental = new IncrementalObservable<>(task, scheduler);

        // We shouldn't start any work until
        assertThat(task.currentSum, equalTo(0));

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = incremental.subscribe(observer);

        // Should get the initial value, unavailable
        assertTrue(observer.getLastValue() == null);

        // Schedule a batch...
        scheduler.runAll();

        // We should be notified with a partial result
        assertThat(observer.getChangeCount(), equalTo(2));
        assertThat(observer.getLastValue(), equalTo(1));

        // Run a few more batches...
        scheduler.runAll();
        scheduler.runAll();
        scheduler.runAll();

        assertThat(observer.getLastValue(), equalTo(3));

    }


    @Test
    public void testCancellation() {

        Adder task = new Adder(3);
        SchedulerStub scheduler = new SchedulerStub();
        Observable<Integer> incremental = new IncrementalObservable<>(task, scheduler);

        // We shouldn't start any work until
        assertThat(task.currentSum, equalTo(0));

        MockObserver<Integer> observer = new MockObserver<>();
        Subscription subscription = incremental.subscribe(observer);

        // Should get the initial value, unavailable
        assertTrue(observer.getLastValue() == null);

        // Schedule a batch...
        scheduler.runAll();

        // We should be notified with a partial result
        assertThat(observer.getChangeCount(), equalTo(2));
        assertThat(observer.getLastValue(), equalTo(1));

        // If we stop listening, no more work should take place...
        subscription.unsubscribe();

        // Run a few more batches...
        scheduler.runAll();
        scheduler.runAll();
        scheduler.runAll();

        assertThat(task.currentSum, equalTo(1));


    }


}
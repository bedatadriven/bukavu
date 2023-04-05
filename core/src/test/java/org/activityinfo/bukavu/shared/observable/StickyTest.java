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

public class StickyTest {

    @Test
    public void sourceLoadsBeforeConnect() {

        PendingValue<Integer> source = new PendingValue<>();
        Observable<Integer> sticky = source.sticky();

        // Underlying source loads...
        source.updateValue(42);

        // Now start listening to sticky
        CountingObserver<Integer> observer = new CountingObserver<>();
        Subscription stickySubscription = sticky.subscribe(observer);

        assertThat(observer.countChanges(), equalTo(1));

        stickySubscription.unsubscribe();
    }

    @Test
    public void sourceAlreadyLoadedOnConstruction() {
        Observable<Integer> source = Observable.just(42);

        Observable<Integer> sticky = source.sticky();

        // Now start listening to sticky
        CountingObserver<Integer> observer = new CountingObserver<>();
        Subscription stickySubscription = sticky.subscribe(observer);

        assertThat(observer.countChanges(), equalTo(1));

        stickySubscription.unsubscribe();
    }

    @Test
    public void valueChangesTwice() {
        PendingValue<Integer> source = new PendingValue<>();
        Observable<Integer> sticky = source.sticky();


        // Now start listening to sticky
        CountingObserver<Integer> observer = new CountingObserver<>();
        Subscription stickySubscription = sticky.subscribe(observer);

        assertThat(observer.isLoaded(), equalTo(false));
        assertThat(observer.countChanges(), equalTo(1));

        // First change
        source.updateValue(42);
        assertThat(observer.isLoaded(), equalTo(true));
        assertThat(observer.getCurrentValue(), equalTo(42));
        assertThat(observer.countChanges(), equalTo(1));

        // Second change
        source.updateValue(43);

        // Sticky should not change again, and no additionl changes should be fired.
        assertThat(observer.isLoaded(), equalTo(true));
        assertThat(observer.getCurrentValue(), equalTo(42));
        assertThat(observer.countChanges(), equalTo(0));

        stickySubscription.unsubscribe();
    }

    @Test
    public void computeOnSubscribeSource() {

        Observable<Integer> source = new Observable<Integer>() {

            @Nullable Integer value = null;

            @Override
            protected void onConnect() {
                value = 42;
                fireChange(42);
            }
        };

        // If no one is listening, no work is done
        Observable<Integer> sticky = source.sticky();

        // Now start listening to sticky
        CountingObserver<Integer> observer = new CountingObserver<>();
        Subscription stickySubscription = sticky.subscribe(observer);
        assertThat(observer.isLoaded(), equalTo(true));
        assertThat(observer.countChanges(), equalTo(1));
    }
}
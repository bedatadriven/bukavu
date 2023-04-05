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

public class ExplicitlyOptimisticObservable<T> extends Observable<MaybeStale<T>> {

    private final Observable<T> observable;
    private @Nullable Subscription subscription;

    public ExplicitlyOptimisticObservable(Observable<T> observable) {
        this.observable = observable;
    }

    @Override
    protected void onConnect() {
        subscription = observable.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T value) {
                maybeUpdateValue(value);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        if(cachedValue != null) {
            cachedValue = cachedValue.outdated();
        }
        assert subscription != null : "onConnect not called";
        subscription.unsubscribe();
        subscription = null;
    }

    private void maybeUpdateValue(@Nullable T value) {
        if(value != null) {
            fireChange(new MaybeStale<>(value, false));

        } else if(cachedValue != null) {
            fireChange(cachedValue.outdated());
        }
    }
}

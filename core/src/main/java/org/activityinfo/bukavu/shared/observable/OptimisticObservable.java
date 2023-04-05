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

import com.google.gwt.core.shared.GwtIncompatible;
import org.checkerframework.checker.nullness.qual.Nullable;

class OptimisticObservable<T> extends Observable<T> {

    private final Observable<T> observable;
    private @Nullable Subscription subscription;


    OptimisticObservable(Observable<T> observable, @Nullable T defaultValue) {
        this.observable = observable;
        this.cachedValue = defaultValue;
    }

    @Override
    protected void onConnect() {
        subscription = observable.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T newValue) {
                // Ignore loading events during refreshes, just keep the previous value
                // until we get an updated value.
                if(newValue != null && newValue != cachedValue) {
                    fireChange(newValue);
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        assert subscription != null : "onConnect not called";
        subscription.unsubscribe();
        subscription = null;
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return observable.waitFor();
    }
}

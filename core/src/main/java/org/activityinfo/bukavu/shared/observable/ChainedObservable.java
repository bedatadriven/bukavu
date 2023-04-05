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

public class ChainedObservable<T> extends Observable<T> {

    private final Observable<Observable<T>> outer;
    private @Nullable Observable<T> inner;
    private @Nullable Subscription outerSubscription = null;
    private @Nullable Subscription innerSubscription = null;

    public ChainedObservable(Observable<Observable<T>> value) {
        this.outer = value;
    }

    @Override
    protected void onConnect() {
        assert outerSubscription == null;

        outerSubscription = outer.subscribe(new Observer<Observable<T>>() {
            @Override
            public void onChange(@Nullable Observable<T> newInner) {
                if(inner != newInner) {
                    unsubscribeFromOldValue();
                    if (newInner == null) {
                        fireChange(null);
                    } else {
                        subscribeToNewValue(newInner);
                    }
                }
            }
        });
    }

    private void unsubscribeFromOldValue() {
        // Unsubscribe from the old value if we had previously been listening
        inner = null;
        if(innerSubscription != null) {
            innerSubscription.unsubscribe();
            innerSubscription = null;
        }
    }

    private void subscribeToNewValue(Observable<T> newInner) {
        inner = newInner;
        innerSubscription = newInner.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T value) {
                ChainedObservable.this.fireChange(value);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        inner = null;
        cachedValue = null;
        if(outerSubscription != null) {
            outerSubscription.unsubscribe();
            outerSubscription = null;
        }
        if(innerSubscription != null) {
            innerSubscription.unsubscribe();
            innerSubscription = null;
        }
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return outer.waitFor().waitFor();
    }
}

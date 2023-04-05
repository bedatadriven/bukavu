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

class Sticky<T> extends Observable<T> {

    private final Observable<T> source;
    private @Nullable Subscription subscription = null;

    Sticky(Observable<T> source) {
        this.source = source;
    }

    @Override
    protected void onConnect() {

        // If *we* already received a value, then nothing to do
        if(cachedValue != null) {
            return;
        }

        // Start listening ... at least until we have a value loaded or disconnected
        this.subscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T value) {
                if(cachedValue == null && value != null) {
                    onLoaded(value);
                }
            }
        });

        // It can happen that the underlying value is computed upon subscription,
        // in which case we need to immediately unsubscribe
        if(cachedValue != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    private void onLoaded(T value) {
        // Once the underlying observable is loaded,
        // we disconnect and don't listen for any further changes.
        if(cachedValue == null) {
            fireChange(value);
        }

        // If the underlying value was computed upon subscription,
        // then we will not yet have stored the subscription
        if(this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    @Override
    protected void onDisconnect() {
        if(this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return source.waitFor();
    }
}

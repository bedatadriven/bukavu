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

import com.google.gwt.user.client.Timer;
import org.checkerframework.checker.nullness.qual.Nullable;

class OptimisticObservableWithTimeout<T> extends Observable<T> {
    private final Observable<T> observable;
    private final int timeoutMillis;

    private @Nullable Subscription subscription;

    private @Nullable Timer timer;

    OptimisticObservableWithTimeout(Observable<T> observable, int timeoutMillis, @Nullable T defaultValue) {
        this.observable = observable;
        this.timeoutMillis = timeoutMillis;
        this.cachedValue = defaultValue;
    }

    @Override
    protected void onConnect() {
        if(timer == null) {
            this.timer = new Timer() {
                @Override
                public void run() {
                    onTimeout();
                }
            };
        }

        subscription = observable.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T newValue) {
                onValueChanged(newValue);
            }
        });
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();

        assert subscription != null : "onConnect was not called";
        subscription.unsubscribe();
        subscription = null;
    }

    private void onValueChanged(@Nullable T newValue) {
        if(newValue != null) {
            if(newValue != cachedValue) {
                if(timer != null && timer.isRunning()) {
                    timer.cancel();
                }
                fireChange(newValue);
            }

        } else {
            // Start the timer... if we don't get a loaded value within the
            // provided timeout, THEN fire then alert observers that we are loading.
            assert timer != null;
            if(!timer.isRunning()) {
                timer.schedule(timeoutMillis);
            }
        }
    }

    private void onTimeout() {
        // We've been optimistic that the value would not be change...
        // but no we've lost our patience.
        fireChange(null);
    }
}

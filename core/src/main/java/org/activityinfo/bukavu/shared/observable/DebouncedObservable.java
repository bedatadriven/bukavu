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
import com.google.gwt.user.client.Timer;
import org.checkerframework.checker.nullness.qual.Nullable;

class DebouncedObservable<T> extends Observable<T> {

    private final Observable<T> source;
    private final int delay;
    private @Nullable Timer timer;
    private @Nullable Subscription sourceSubscription;
    private @Nullable T pendingValue;

    public DebouncedObservable(Observable<T> source, int delay) {
        this.source = source;
        this.delay = delay;
    }

    @Override
    protected void onConnect() {

        sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T newValue) {
                if(isConnecting()) {
                    DebouncedObservable.this.fireChange(newValue);
                } else {
                    if(timer == null) {
                        timer = new Timer() {
                            @Override
                            public void run() {
                                DebouncedObservable.this.fireChange(pendingValue);
                            }
                        };
                    }
                    pendingValue = newValue;
                    if (timer.isRunning()) {
                        timer.cancel();
                    }
                    timer.schedule(delay);
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        cachedValue = null;
        pendingValue = null;
        timer = null;
        if(sourceSubscription != null) {
            sourceSubscription.unsubscribe();
            sourceSubscription = null;
        }
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return source.waitFor();
    }
}

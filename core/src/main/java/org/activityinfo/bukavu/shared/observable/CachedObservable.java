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

class CachedObservable<T> extends Observable<T> {

    private final Observable<T> source;
    private final CachePredicate<T> cachePredicate;

    private @Nullable Subscription sourceSubscription;

    public CachedObservable(Observable<T> source, CachePredicate<T> cachePredicate) {
        this.source = source;
        this.cachePredicate = cachePredicate;
    }

    @Override
    protected void onConnect() {
        this.sourceSubscription = source.subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T newValue) {
                if (cachedValue == null) {
                    if(newValue != null) {
                        fireChange(newValue);
                    }
                } else if(newValue == null) {
                    fireChange(newValue);
                } else if(!cachePredicate.isSame(cachedValue, newValue)) {
                    fireChange(newValue);
                }
            }
        });
    }

    @Override
    protected void onDisconnect() {
        assert sourceSubscription != null : "onConnect not called";
        sourceSubscription.unsubscribe();
        sourceSubscription = null;
    }


    @Override
    @GwtIncompatible
    public T waitFor() {
        return source.waitFor();
    }
}

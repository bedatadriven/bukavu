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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GwtIncompatible;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ObservableComputedMap<K, T> extends Observable<Map<K, Observable<T>>> {

    private final Observable<? extends Collection<K>> keys;
    private final Map<K, Observable<T>> cache = new HashMap<>();
    private final Function<K, Observable<T>> function;

    private @Nullable Subscription keySubscription;

    public ObservableComputedMap(Observable<? extends Collection<K>> keys, Function<K, Observable<T>> function) {
        this.keys = keys;
        this.function = function;
    }

    @Override
    protected void onConnect() {
        keySubscription = keys.subscribe(this::keysChanged);
    }

    private void keysChanged(@Nullable Collection<K> newKeys) {
        if(newKeys == null) {
            // Keys are still loading, so our result is still loading too
            fireChange(null);
            return;
        }
        if(cachedValue != null && setsEqual(newKeys, cachedValue.keySet())) {
            return;
        }
        Map<K, Observable<T>> newMap = new HashMap<>();
        for (K newKey : newKeys) {
            newMap.put(newKey, cache.computeIfAbsent(newKey, k -> function.apply(k)));
        }

        // Signal that we have a new value
        fireChange(newMap);

        // Sweep cache
        cache.keySet().removeIf(oldKey -> oldKey != null && !newKeys.contains(oldKey));
    }

    private boolean setsEqual(Collection<K> a, Set<? extends K> b) {
        if(a.size() != b.size()) {
            return false;
        }
        return b.containsAll(a);
    }

    @Override
    protected void onDisconnect() {
        if(keySubscription != null) {
            keySubscription.unsubscribe();
        }
        if(GWT.isScript()) {
            Scheduler.get().scheduleDeferred(() -> {
                if(!isConnected()) {
                    cache.clear();
                }
            });
        } else {
            cache.clear();
        }
    }

    @GwtIncompatible
    @Override
    public Map<K, Observable<T>> waitFor() {
        Map<K, Observable<T>> newMap = new HashMap<>();
        for (K key : keys.waitFor()) {
            newMap.put(key, function.apply(key));
        }
        return newMap;
    }
}

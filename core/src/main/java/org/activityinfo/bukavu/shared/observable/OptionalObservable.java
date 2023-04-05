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

import java.util.Optional;

class OptionalObservable<T> extends Observable<Optional<T>> {

    private final Observable<T> source;
    private @Nullable Subscription sourceSubscription;

    OptionalObservable(Observable<T> source) {
        this.source = source;
        this.cachedValue = Optional.empty();
    }

    @Override
    protected void onConnect() {
        sourceSubscription = source.subscribe(this::onSourceChange);
    }

    private void onSourceChange(@Nullable T value) {
        fireChange(Optional.ofNullable(value));
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        if(sourceSubscription != null) {
            sourceSubscription.unsubscribe();
            sourceSubscription = null;
        }
    }
}

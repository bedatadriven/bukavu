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

import java.util.function.Function;

public class ConstantObservable<T> extends Observable<T> {
    
    private final T value;

    public ConstantObservable(T value) {
        this.value = value;
        this.cachedValue = value;
    }

    @Override
    public <R> Observable<R> transform(Function<T, R> function) {
        return just(function.apply(value));
    }

    @Override
    public <R> Observable<R> join(Function<T, Observable<R>> function) {
        return function.apply(value);
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return value;
    }
}

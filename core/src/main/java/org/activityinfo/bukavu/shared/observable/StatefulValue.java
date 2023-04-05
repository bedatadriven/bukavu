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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class StatefulValue<T> extends Observable<T> implements Consumer<T> {
    
    private T value;

    public StatefulValue(T value) {
        this.value = value;
        this.cachedValue = value;
    }

    public T update(Function<T, T> function) {
        updateValue(function.apply(value));
        return value;
    }

    /**
     * Updates the reference to this state's value and notifies subscribers.
     */
    public void updateValue(T value) {
        this.value = value;
        fireChange(value);
    }

    @Override
    protected void onConnect() {
        fireChange(value);
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        return value;
    }

    /**
     * If the given {@code newValue} is not equal to the current value,
     * update the current value, fire a changed event, and return {@code true}.
     *
     * <p>If the given {@code newValue} <em>is</em> equal to the current value, do nothing and
     * return {@code false}</p>
     * @param newValue
     * @return
     */
    public boolean updateIfNotEqual(T newValue) {
        if (!Objects.equals(this.value, newValue)) {
            this.value = newValue;
            fireChange(newValue);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateIfNotSame(T value) {
        if(this.value != value) {
            this.value = value;
            fireChange(value);
            return true;
        } else {
            return false;
        }
    }

    public T get() {
        return value;
    }

    @Override
    public void accept(T t) {
        updateValue(t);
    }
}

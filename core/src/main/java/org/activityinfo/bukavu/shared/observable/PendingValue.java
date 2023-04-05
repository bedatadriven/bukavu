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

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Holder for a changing value that can also be loaded.
 * @param <T>
 */
public class PendingValue<T> extends Observable<T> implements Consumer<T> {

    private @Nullable T value;

    public PendingValue() {
        value = null;
    }

    public PendingValue(T value) {
        this.value = value;
        this.cachedValue = value;
    }

    /**
     * Updates the reference to this state's value and notifies subscribers.
     */
    public void updateValue(@Nullable T value) {
        this.value = value;
        fireChange(value);
    }

    public void clear() {
        fireChange(null);
    }

    @Override
    protected void onConnect() {
        fireChange(value);
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        if(value == null) {
            throw new IllegalStateException();
        }
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

    @SuppressWarnings("deprecation")
    public @Nullable T get() {
        return value;
    }

    @Override
    public void accept(T t) {
        updateValue(t);
    }
}

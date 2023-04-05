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

import java.util.Objects;

/**
 * Wrapper for a value that <i>may</i> be out of date.
 */
public class MaybeStale<T> {
    private final boolean stale;
    private final T value;

    MaybeStale(T value, boolean stale) {
        this.stale = stale;
        this.value = value;
    }

    public boolean isStale() {
        return stale;
    }

    public T getValue() {
        return value;
    }

    MaybeStale<T> outdated() {
        if(stale) {
            return this;
        } else {
            return new MaybeStale<>(value, true);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaybeStale<?> maybeStale1 = (MaybeStale<?>) o;
        return stale == maybeStale1.stale &&
                Objects.equals(value, maybeStale1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stale, value);
    }
}

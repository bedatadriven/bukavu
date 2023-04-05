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

public class CountingObserver<T> implements Observer<T> {

    private int changeCount = 0;
    private @Nullable T currentValue;

    @Override
    public void onChange(@Nullable T value) {
        System.out.println("Changed.");
        currentValue = value;
        changeCount++;
    }

    public boolean isLoaded() {
        return currentValue != null;
    }

    /**
     * @return the number of change notifications since the last call to {@code countChanges()}
     */
    public int countChanges() {
        int count = changeCount;
        changeCount = 0;
        return count;
    }

    public T getCurrentValue() {
        if(currentValue == null) {
            throw new AssertionError("Values has not yet loaded");
        }
        return currentValue;
    }
}

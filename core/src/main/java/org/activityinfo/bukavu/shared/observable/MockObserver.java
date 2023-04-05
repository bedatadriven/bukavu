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

public class MockObserver<T> implements Observer<T> {
    private int changeCount;
    private @Nullable T lastValue;
    private boolean lastLoading;
    
    @Override
    public void onChange(@Nullable T value) {
        changeCount++;
        if(value != null) {
            this.lastValue = value;
            this.lastLoading = false;
        } else {
            this.lastValue = null;
            this.lastLoading = true;
        }
    }
    
    public void resetCount() {
        changeCount = 0;
    }

    public int getChangeCount() {
        return changeCount;
    }

    public void assertChangeFiredOnce() {
        assertFired(1);
    }

    public void assertFired(int expectedCount) {
        if(changeCount == 0) {
            throw new AssertionError("onChange() has not been called.");
        }
        if(changeCount != expectedCount) {
            throw new AssertionError("onChange() was called " + changeCount + " times, expected " + expectedCount);
        }
        changeCount = 0;
    }

    public void assertChangeNotFired() {
        if(changeCount > 0) {
            throw new AssertionError("No call to onChange() expected; there have been " + changeCount + " call(s).");
        }
    }

    public @Nullable T getLastValue() {
        return lastValue;
    }

    public boolean isLoading() {
        return lastLoading;
    }

    public boolean isLoaded() {
        return !isLoading();
    }

    public void assertValueEquals(T value) {
        if(lastValue == null) {
            throw new AssertionError("Expected: " + value + " but was not loaded");
        }
        if(!lastValue.equals(value)) {
            throw new AssertionError("Expected: " + value + " but was " + lastValue);
        }
    }

    public void assertLoading() {
        if(lastValue != null) {
            throw new AssertionError("Expected loading, but found: " + lastValue);
        }
    }
}

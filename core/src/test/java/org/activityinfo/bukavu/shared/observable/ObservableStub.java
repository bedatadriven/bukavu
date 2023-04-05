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

public class ObservableStub<T> extends Observable<T> {
    
    private @Nullable T value;

    public ObservableStub() {
    }

    public ObservableStub(T initialValue) {
        this.value = initialValue;
    }

    public void setToLoading() {
        if(value != null) {
            value = null;
            fireChange(null);
        }
    }
    
    public void updateValue(T value) {
        this.value = value;
        fireChange(value);
    }

    @Override
    protected void onConnect() {
        fireChange(this.value);
    }

}

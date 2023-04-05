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
package org.activityinfo.bukavu.shared.tree;

import org.activityinfo.bukavu.shared.observable.Observable;

import java.util.function.Function;

public class ReactiveProps<T> implements VComponentProps {
    private String debugId;
    private Observable<T> value;
    private VNode loadingIndicator;
    private Function<T, VNode> renderer;

    public ReactiveProps(String debugId, Observable<T> value, Function<T, VNode> renderer, VNode loadingIndicator) {
        this.debugId = debugId;
        this.value = value;
        this.loadingIndicator = loadingIndicator;
        this.renderer = renderer;
    }

    public Observable<T> getValue() {
        return value;
    }

    public VNode getLoadingIndicator() {
        return loadingIndicator;
    }

    public Function<T, VNode> getRenderer() {
        return renderer;
    }

    public String getDebugId() {
        return debugId;
    }

}

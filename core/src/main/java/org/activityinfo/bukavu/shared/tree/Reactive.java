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

import org.activityinfo.bukavu.client.VDom;
import org.activityinfo.bukavu.shared.observable.Observable;

import java.util.function.Function;

public class Reactive {

    private static String findDebugId() {
        if(VDom.isDevMode()){
            return new Error().getStackTrace()[4].getMethodName();
        }
        return null;
    }

    public static VNode create(Observable<VNode> observable) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(findDebugId(), observable, Function.identity(), null));
    }

    /**
     * Creates a new Reactive component.
     *
     * <p>This factory method accepts an observable value of any type, and a rendering function which can transform
     * the Observable of type {@code T} to a {@code VNode}. If the value does not change, as judged by referential equality,
     * then {@code renderer} will not be called again.</p>
     *
     * @param observable an Observable value
     * @param renderer a function to render the observable value as a VNode.
     * @param <T> the type of Observable value
     * @return a {@code VComponent} node.
     */
    public static <T> VNode create(Observable<T> observable, Function<T, VNode> renderer) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(findDebugId(), observable, renderer, null));
    }

    /**
     * Creates a new Reactive component.
     *
     * <p>This factory method accepts an observable value of any type, and a rendering function which can transform
     * the Observable of type {@code T} to a {@code VNode}. If the value does not change, as judged by referential equality,
     * then {@code renderer} will not be called again.</p>
     *
     * @param observable an Observable value
     * @param renderer a function to render the observable value as a VNode.
     * @param loadingIndicator a loading indicator to use while waiting for the observable value.
     * @param <T> the type of Observable value
     * @return a {@code VComponent} node.
     */
    public static <T> VNode create(Observable<T> observable, Function<T, VNode> renderer, VNode loadingIndicator) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(findDebugId(), observable, renderer, loadingIndicator));
    }

    public static VNode create(Observable<VNode> observable, VNode loadingIndicator) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(null, observable, Function.identity(), loadingIndicator));
    }

    public static VNode create(String debugId, Observable<VNode> observable) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(debugId, observable, Function.identity(), null));
    }

    public static <T> VNode create(String debugId, Observable<T> observable, Function<T, VNode> renderer) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(debugId, observable, renderer, null));
    }

    public static VNode create(String debugId, Observable<VNode> observable, VNode loadingIndicator) {
        return new VComponent(ReactiveComponent.CONSTRUCTOR, new ReactiveProps<>(debugId, observable, Function.identity(), loadingIndicator));
    }
}

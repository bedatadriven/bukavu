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

import org.activityinfo.bukavu.shared.html.H;
import org.activityinfo.bukavu.shared.observable.Observer;
import org.activityinfo.bukavu.shared.observable.Subscription;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Logger;

public class ReactiveComponent<T> extends VComponentInstance {

    public static final VComponentConstructor CONSTRUCTOR = ReactiveComponent::new;

    private static final Logger LOGGER = Logger.getLogger(ReactiveComponent.class.getName());

    private ReactiveProps<T> props;

    private Subscription subscription;

    private T value = null;

    private VNode content = null;


    public ReactiveComponent(VComponentProps props) {
        this.props = (ReactiveProps<T>) props;
        this.content = ((ReactiveProps<T>) props).getLoadingIndicator();
        if(this.content == null) {
            this.content = H.div();
        }
    }

    @Override
    public boolean canAcceptProps(VComponent vnode) {
        return vnode.getConstructor() == CONSTRUCTOR;
    }

    @Override
    public boolean updateProps(VComponentProps newPropsObj) {
        ReactiveProps<T> oldProps = this.props;
        this.props = (ReactiveProps<T>) newPropsObj;

        if((oldProps.getValue() != props.getValue() ||
                oldProps.getRenderer() != props.getRenderer()) && subscription != null) {
            subscription.unsubscribe();
            subscribe();
        }

        // Always return false, because subscribe() will trigger a refresh if needed.
        return false;
    }

    @Override
    public VNode render() {
        return content;
    }

    @Override
    public void componentDidMount() {
        subscribe();
    }

    private void subscribe() {
        subscription = props.getValue().subscribe(new Observer<T>() {
            @Override
            public void onChange(@Nullable T newValue) {
                if(newValue == null) {
                    if(props.getLoadingIndicator() != null && content != props.getLoadingIndicator()) {
                        content = props.getLoadingIndicator();
                        value = null;
                        ReactiveComponent.this.refresh();
                    }
                } else {
                    if(newValue != value) {
                        value = newValue;
                        content = props.getRenderer().apply(value);
                        ReactiveComponent.this.refresh();
                    }
                }
            }
        });
    }

    @Override
    public void componentWillUnmount() {
        if(subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public String toString() {
        return "Reactive[" + props.getDebugId() + "]";
    }
}

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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract class ComputedObservable<T> extends Observable<T> {

    private static final Logger LOGGER = Logger.getLogger(ComputedObservable.class.getName());

    private final Scheduler scheduler;
    private final Observable[] arguments;
    private final @Nullable Object[] argumentValues;
    private final @Nullable Subscription @NonNull [] subscriptions;

    public ComputedObservable(Scheduler scheduler, Observable... arguments) {
        this.scheduler = scheduler;
        this.arguments = arguments;
        this.argumentValues = new Object[arguments.length];
        this.subscriptions = new Subscription[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            assert arguments[i] != null : "argument " + i + " is null.";
        }
    }

    public ComputedObservable(Scheduler scheduler, List<Observable<?>> arguments) {
        this(scheduler, arguments.toArray(new Observable[arguments.size()]));
    }

    private class ArgumentListener implements Observer<Object> {
        private final int argumentIndex;

        private ArgumentListener(int argumentIndex) {
            this.argumentIndex = argumentIndex;
        }

        @Override
        public void onChange(@Nullable Object newValue) {
            if(isConnecting()) {
                argumentValues[argumentIndex] = newValue;
                return;
            }

            Object oldValue = argumentValues[argumentIndex];
            if(newValue == null) {
                argumentValues[argumentIndex] = null;
                fireChange(null);
            } else if(oldValue != newValue) {
                argumentValues[argumentIndex] = newValue;
                maybeScheduleRecompute();
            }
        }
    }

    private void maybeScheduleRecompute() {
        scheduler.schedule(() -> {
            recompute();
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onConnect() {

        @Nullable Object @Nullable [] oldArguments = null;
        if(cachedValue != null) {
            oldArguments = Arrays.copyOf(argumentValues, argumentValues.length);
        }

        for (int i = 0; i < subscriptions.length; ++i) {
            assert subscriptions[i] == null;
            subscriptions[i] = arguments[i].subscribe(new ArgumentListener(i));
        }

        // Check to see if we need to recompute
        if(oldArguments != null) {
            maybeRecompute(oldArguments);
        } else {
            recompute();
        }
    }

    void maybeRecompute(@Nullable Object[] oldValues) {
        boolean argumentsChanged = false;
        for (int i = 0; i < oldValues.length; i++) {
            if(oldValues[i] == null || argumentValues[i] == null) {
                return;
            }
            if(oldValues[i] != argumentValues[i]) {
                argumentsChanged = true;
            }
        }
        if(argumentsChanged) {
            recompute();
        }
    }

    @SuppressWarnings("nullness:argument")
    void recompute() {

        // Check if we have all argumentValues
        if(cachedValue == null) {
            for (int i = 0; i < arguments.length; i++) {
                if(argumentValues[i] == null) {
                    // Still loading, still no value
                    return;
                }
            }
        }

        // Ok, everything loaded, compute the new result

        // Note that transformation functions are NOT MEANT to throw
        // exceptions. Any error or failure should be encoded in the value type,
        // for example, using Maybe<> or Result<> or Optional<>

        // If the function STILL throws an exception, the only thing we can do
        // at this stage is log the error and treat the result as still loading.

        @Nullable T newResult;
        try {
            newResult = compute(argumentValues);
            assert newResult != null : "Result of computation was null!";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception thrown by observable", e);
            newResult = null;
        }

        fireChange(newResult);
    }

    protected abstract T compute(Object[] arguments);

    @Override
    protected void onDisconnect() {
        // Unsubscribe from all of our arguments
        for(int i=0;i<subscriptions.length;++i) {
            Subscription s = subscriptions[i];
            assert s != null : "subscription already unsubscribed!";
            s.unsubscribe();
            subscriptions[i] = null;
        }
    }

    @Override
    @GwtIncompatible
    public T waitFor() {
        Object[] argumentValues = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            argumentValues[i] = arguments[i].waitFor();
        }
        return compute(argumentValues);
    }
}

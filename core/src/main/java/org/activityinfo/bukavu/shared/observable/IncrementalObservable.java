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

public class IncrementalObservable<T> extends Observable<T> {

    private final IncrementalTask<T> task;
    private final Scheduler scheduler;
    private boolean completed = false;

    public IncrementalObservable(IncrementalTask<T> task, Scheduler scheduler) {
        this.task = task;
        this.scheduler = scheduler;
    }

    @Override
    protected void onConnect() {
        if(!completed) {
            scheduleNextSlice();
        }
    }

    private void scheduleNextSlice() {
        scheduler.schedule(() -> {
            if(!isConnected() && !isConnecting()) {
                return;
            }

            fireChange(task.execute());

            if(task.isDone()) {
                completed = true;
            } else {
                scheduleNextSlice();
            }
        });
    }
}

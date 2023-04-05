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

import com.google.gwt.core.shared.GWT;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DebouncingEventLoopScheduler implements Scheduler {

    private @Nullable Runnable scheduled = null;

    @Override
    public void schedule(Runnable runnable) {
        // If running in a unit test, behave like a synchronous scheduler
        if(!GWT.isScript()) {
            runnable.run();
            return;
        }

        // Otherwise, check if something is already scheduled
        if(scheduled == null) {
            scheduled = runnable;
            com.google.gwt.core.client.Scheduler.get().scheduleDeferred(() -> execute());
        } else {
            // We are already scheduled-- update the task and wait for it to execute
            scheduled = runnable;
        }
    }

    private void execute() {
        if(scheduled != null) {
            scheduled.run();
        }
        scheduled = null;
    }
}

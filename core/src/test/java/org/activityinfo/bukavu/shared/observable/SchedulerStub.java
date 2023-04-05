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

import java.util.ArrayList;
import java.util.List;


public class SchedulerStub implements Scheduler {
    
    private List<Runnable> queue = new ArrayList<>();

    @Override
    public void schedule(Runnable runnable) {
        queue.add(runnable);
    }

    public void runAll() {
        List<Runnable> toRun = new ArrayList<>(queue);
        queue.clear();
        for (Runnable task : toRun) {
            task.run();
        }
    }
}

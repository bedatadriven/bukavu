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
package org.activityinfo.bukavu.client;

import com.google.gwt.core.client.Scheduler;
import elemental2.core.JsArray;
import org.activityinfo.bukavu.shared.tree.VComponentInstance;

public class RenderQueue {

    /**
     * Managed queue of dirty components to be re-rendered
     */
    private static final JsArray<VComponentInstance> ITEMS = new JsArray<>();

    private static final JsArray<Runnable> finishedRenderingCallbacks = new JsArray<>();

    private static boolean scheduled = false;

    /**
     * Enqueue a rerender of a component
     * @param component The component to rerender
     */
    public static void enqueueRender(VComponentInstance component) {
        if (!component.dirty && (component.dirty = true)) {
            ITEMS.push(component);
            scheduleRerender();
        }
    }

    public static void scheduleRerender() {
        if(!scheduled) {
            scheduled = true;
            Scheduler.get().scheduleDeferred(() -> rerender());
        }
    }

    public static void whenFinishedRendering(Runnable runnable) {
        finishedRenderingCallbacks.push(runnable);
        scheduleRerender();
    }

    /**
     *  Rerender all enqueued dirty components
     */
    private static void rerender() {
        scheduled = false;
        VComponentInstance p;
        while ( (p = ITEMS.pop()) != null) {
            if (p.dirty) {
                Diff.renderComponent(p, null, false);
            }
        }

        Runnable callback;
        while ( (callback = finishedRenderingCallbacks.pop()) != null) {
            callback.run();
        }
    }
}

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

public interface IncrementalTask<T> {

    /**
     * Executes some part of the work to be done.
     *
     * <p>Some tasks may return intermediate results, even if the task is not completely finished.</p>
     *
     * <p>If no result, even an intermediate result, is available, this method should return {@code null}</p>
     *
     * <p>If an intermediate result is to be returned, it must be immutable.</p>
     *
     */
    @Nullable T execute();

    /**
     *
     * @return true if this task is complete, or {@code false} if {@code execute()} should be invoked again
     */
    boolean isDone();

}

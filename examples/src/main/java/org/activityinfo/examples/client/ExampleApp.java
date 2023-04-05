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
package org.activityinfo.examples.client;

import com.google.gwt.core.client.EntryPoint;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import org.activityinfo.bootstrap.client.Bootstrap;
import org.activityinfo.bootstrap.client.ButtonBuilder;
import org.activityinfo.bukavu.client.Diff;
import org.activityinfo.bukavu.shared.html.H;
import org.activityinfo.bukavu.shared.tree.VNode;

public class ExampleApp implements EntryPoint {
    @Override
    public void onModuleLoad() {
        Element container = DomGlobal.document.createElement("div");
        DomGlobal.document.body.appendChild(container);
        Diff.render(renderHelloWorld(), container);
        DomGlobal.alert("Hello world!");
    }

    private VNode renderHelloWorld() {
        return H.div(
            Bootstrap.button("Hello")
                .style(ButtonBuilder.Style.PRIMARY)
                .build(),
            Bootstrap.button("World")
                .style(ButtonBuilder.Style.DANGER)
                .outline(true)
                .build());
    }
}

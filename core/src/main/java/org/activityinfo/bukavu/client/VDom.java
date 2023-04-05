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

import elemental2.dom.Element;
import jsinterop.base.Js;
import org.activityinfo.bukavu.shared.html.H;
import org.activityinfo.bukavu.shared.tree.VNode;

public class VDom {

    /**
     * Updates the browser's DOM to match the supplied vnode.
     *
     * <p>If the {@code domParent} already has a child, it is updated to match the
     * supplied {@code vnode}</p>
     *
     * @param domParent The DOM element to which the vnode should be added/updated.
     * @param vdomNode the virtual DOM element
     */
    public static void updateDom(Element domParent, VNode vnode) {

        Element child = domParent.firstElementChild;

        Diff.diff(Js.uncheckedCast(child), vnode, Js.uncheckedCast(domParent), false);
    }

    /**
     * Unmounts all components and removes all children.
     */
    public static void tearDown(Element domParent) {
        updateDom(domParent, H.div());
        domParent.remove();
    }

    public static boolean isDevMode() {
        return System.getProperty("superdevmode").equals("on");
    }
}

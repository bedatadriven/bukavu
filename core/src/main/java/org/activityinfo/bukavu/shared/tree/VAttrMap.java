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

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import elemental2.dom.DragEvent;
import elemental2.dom.Event;
import elemental2.dom.InputEvent;
import elemental2.dom.MouseEvent;

import java.util.Set;

public interface VAttrMap {

    VAttrMap EMPTY = VAttr.create();

    VAttrMap set(String propertyName, Object value);

    VAttrMap set(String propertyName, double value);

    Object get(String propertyName);

    void addClassName(String newClass);

    VAttrMap addClassName(String className, boolean add);

    VAttrMap data(String dataPropertyName, String value);

    VAttrMap href(SafeUri uri);

    VAttrMap setId(String id);

    VAttrMap setClass(String classNames);

    VAttrMap setClass(String className, boolean add);

    VAttrMap setStyle(Style style);

    VAttrMap setStyle(VAttrMap style);

    VAttrMap setTitle(String title);

    VAttrMap setInnerHtml(SafeHtml html);

    VAttrMap disabled(boolean disabled);

    VAttrMap onclick(EventHandler<MouseEvent> handler);

    VAttrMap oninput(EventHandler<InputEvent> handler);

    VAttrMap ondragover(EventHandler<DragEvent> handler);

    VAttrMap ondragleave(EventHandler<DragEvent> handler);

    VAttrMap ondrop(EventHandler<DragEvent> handler);

    <T extends Event> VAttrMap on(String eventName, EventHandler<T> eventHandler);

    EventHandler getEventHandler(String eventName);

    VAttrMap draggable(boolean draggable);

    VAttrMap placeholder(String text);

    VAttrMap setData(String name, String value);

    @GwtIncompatible
    Set<String> keys();

    boolean isEmpty();

}

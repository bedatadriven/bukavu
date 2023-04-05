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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import elemental2.dom.DragEvent;
import elemental2.dom.InputEvent;
import elemental2.dom.MouseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Node Property Map.
 *
 * <p>Important: an alternate implementation is provided in the translatable/ source root
 * that will allow the GWT compiler to map this to a simple Javascript object. If you add accessor/setter methods here,
 * they must also be copied to the translatable version.
 * </p>
 */
final class VAttrMapJre implements VAttrMap {


    private final Map<String, Object> propMap = new HashMap<>();

    VAttrMapJre() {
        if(GWT.isScript()) {
            throw new IllegalStateException("super sources are not being used.");
        }
    }

    @Override
    public VAttrMap set(String propertyName, Object value) {
        propMap.put(propertyName, value);
        return this;
    }

    @Override
    public VAttrMap set(String propertyName, double value) {
        return set(propertyName, Double.toString(value));
    }

    @Override
    public Object get(String propertyName) {
        return propMap.get(propertyName);
    }

    private void remove(String propertyName) {
        propMap.remove(propertyName);
    }


    @GwtIncompatible
    public Set<String> keys() {
        return propMap.keySet();
    }

    @Override
    public boolean isEmpty() {
        return propMap.isEmpty();
    }

    /*================= Below this point the Java and Javascript versions are the same ===================*/


    public static final VAttrMap EMPTY = VAttr.create();


    @Override
    public void addClassName(String newClass) {
        String classNameValue = (String) get("className");
        if(classNameValue == null) {
            set("className", newClass);
        } else {
            set("className", classNameValue + " " + newClass);
        }
    }

    @Override
    public VAttrMap addClassName(String className, boolean add) {
        if(add) {
            addClassName(className);
        }
        return this;
    }

    /**
     * Sets the data-{dataPropertyName} property to the given value.
     */
    @Override
    public VAttrMap data(String dataPropertyName, String value) {
        return set("data-" + dataPropertyName, value);
    }

    @Override
    public VAttrMap href(SafeUri uri) {
        return set("href", uri.asString());
    }

    /**
     * Sets the "id" property
     */
    @Override
    public VAttrMap setId(String id) {
        return set("id", id);
    }


    @Override
    public VAttrMap setClass(String classNames) {
        return set("className", classNames);
    }

    @Override
    public VAttrMap setClass(String className, boolean add) {
        if(add) {
            setClass(className);
        }
        return this;
    }

    public static boolean isObject(Object object) {
        return object instanceof VAttrMapJre;
    }

    @Override
    public VAttrMap setStyle(Style style) {
        set("style", style.asPropMap());
        return this;
    }

    @Override
    public VAttrMap setStyle(VAttrMap style) {
        return set("style", style);
    }

    @Override
    public VAttrMap setTitle(String title) {
        return set("title", title);
    }

    @Override
    public VAttrMap setInnerHtml(SafeHtml html) {
        VAttrMap propMap = VAttr.create();
        propMap.set("__html", html.asString());

        return set("dangerouslySetInnerHTML", propMap);
    }

    @Override
    public VAttrMap disabled(boolean disabled) {
        if(disabled) {
            set("disabled", "true");
        } else {
            remove("disabled");
        }
        return this;
    }

    @Override
    public VAttrMap onclick(EventHandler<MouseEvent> handler) {
        return set("onclick", handler);
    }

    @Override
    public VAttrMap oninput(EventHandler<InputEvent> handler) {
        return set("oninput", handler);
    }

    @Override
    public VAttrMap ondragover(EventHandler<DragEvent> handler) {
        return set("ondragover", handler);
    }

    @Override
    public VAttrMap ondragleave(EventHandler<DragEvent> handler) {
        return set("ondragleave", handler);
    }

    @Override
    public VAttrMap ondrop(EventHandler<DragEvent> handler) {
        return set("ondrop", handler);
    }

    @Override
    public VAttrMap on(String eventName, EventHandler eventHandler) {
        set("on" + eventName, eventHandler);
        return this;
    }

    @Override
    public EventHandler getEventHandler(String eventName) {
        return null;
    }

    @Override
    public VAttrMap draggable(boolean draggable) {
        return set("draggable", draggable ? "true" : "false");
    }

    @Override
    public VAttrMap placeholder(String text) {
        return set("placeholder", text);
    }

    @Override
    public VAttrMap setData(String name, String value) {
        return set("data-" + name, value);
    }
}

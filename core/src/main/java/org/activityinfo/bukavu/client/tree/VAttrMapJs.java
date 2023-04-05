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
package org.activityinfo.bukavu.client.tree;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import elemental2.dom.DragEvent;
import elemental2.dom.InputEvent;
import elemental2.dom.MouseEvent;
import org.activityinfo.bukavu.shared.tree.EventHandler;
import org.activityinfo.bukavu.shared.tree.Style;
import org.activityinfo.bukavu.shared.tree.VAttr;
import org.activityinfo.bukavu.shared.tree.VAttrMap;

import java.util.Set;

/**
 * Node Property Map.
 *
 * <p>This is a specialized version that translates to a single Javascript object.</p>
 */
public final class VAttrMapJs extends JavaScriptObject implements VAttrMap {

    protected VAttrMapJs() {
    }

    public static native VAttrMapJs create() /*-{
        return {};
    }-*/;

    public native VAttrMapJs set(String propertyName, Object value) /*-{
        this[propertyName] = value;
        return this;
    }-*/;

    @Override
    public VAttrMap set(String propertyName, double value) {
        return set(propertyName, Double.toString(value));
    }

    public native Object get(String propertyName) /*-{
        return this[propertyName];
    }-*/;

    private native void remove(String propertyName) /*-{
        delete this[propertyName];
    }-*/;

    @Override
    @GwtIncompatible
    public Set<String> keys() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public native boolean isEmpty() /*-{
        for(var prop in this) {
            if(this.hasOwnProperty(prop))
                return false;
        }
        return true;
    }-*/;

    /*================= Below this point the Java and Javascript versions are the same ===================*/


    /**
     * Creates a new {@code PropMapJs} with the given style object.
     */
    public static VAttrMapJs withStyle(Style style) {
        VAttrMapJs propMap = create();
        propMap.setStyle(style);
        return propMap;
    }

    /**
     * Creates a new {@code PropMapJs} with the given value for the {@code className} property
     */
    public static VAttrMapJs withClasses(String classes) {
        return create().setClass(classes);
    }


    public void addClassName(String newClass) {
        String classNameValue = (String) get("className");
        if(classNameValue == null) {
            set("className", newClass);
        } else {
            set("className", classNameValue + " " + newClass);
        }
    }

    public VAttrMapJs addClassName(String className, boolean add) {
        if(add) {
            addClassName(className);
        }
        return this;
    }


    /**
     * Sets the data-{dataPropertyName} property to the given value.
     */
    public VAttrMapJs data(String dataPropertyName, String value) {
        return set("data-" + dataPropertyName, value);
    }

    public VAttrMapJs href(SafeUri uri) {
        return set("href", uri.asString());
    }

    /**
     * Sets the "id" property
     */
    public VAttrMapJs setId(String id) {
        return set("id", id);
    }


    public VAttrMapJs setClass(String classNames) {
        return set("className", classNames);
    }

    public VAttrMapJs setClass(String className, boolean add) {
        if(add) {
            setClass(className);
        }
        return this;
    }

    public static boolean isObject(Object object) {
        return object instanceof VAttrMapJs;
    }

    public VAttrMapJs setStyle(Style style) {
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

    public VAttrMapJs disabled(boolean disabled) {
        if(disabled) {
            set("disabled", "true");
        } else {
            remove("disabled");
        }
        return this;
    }

    public VAttrMapJs onclick(EventHandler<MouseEvent> handler) {
        return set("onclick", handler);
    }

    public VAttrMapJs oninput(EventHandler<InputEvent> handler) {
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


    public EventHandler getEventHandler(String eventName) {
        return null;
    }

    public VAttrMapJs draggable(boolean draggable) {
        return set("draggable", draggable ? "true" : "false");
    }

    public VAttrMapJs placeholder(String text) {
        return set("placeholder", text);
    }

    @Override
    public VAttrMap setData(String name, String value) {
        return set("data-" + name, value);
    }
}

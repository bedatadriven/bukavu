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

import com.google.gwt.core.client.JavaScriptObject;
import org.activityinfo.bukavu.shared.tree.VAttr;
import org.activityinfo.bukavu.shared.tree.VAttrMap;

public class Properties {


    public static void apply(DomNode dom, VAttrMap attrs, boolean isSvgMode) {
        diffProperties(dom, attrs, VAttr.create(), isSvgMode);
    }

    /**
     * Apply differences in attributes from a VNode to the given DOM Element.
     * @param  dom Element with attributes to diff `attrs` against
     * @param  attrs The desired end-state key-value attribute pairs
     * @param  old Current/previous attributes (from previous VNode or
     *  element's prop cache)
     */
    public static native void diffProperties(DomNode dom, VAttrMap attrs, VAttrMap old, boolean isSvgMode) /*-{
        var name;

        // remove attributes no longer present on the vnode by setting them to undefined
        for (name in old) {
            if (!(attrs && attrs[name]!=null) && old[name]!=null) {
                @Properties::setAccessor(*)(dom, name, old[name], old[name] = undefined, isSvgMode);
            }
        }

        // add new & update changed attributes
        for (name in attrs) {
            if (name!=='children' && name!=='innerHTML' && (!(name in old) || attrs[name]!==(name==='value' || name==='checked' ? dom[name] : old[name]))) {
                @Properties::setAccessor(*)(dom, name, old[name], old[name] = attrs[name], isSvgMode);
            }
        }
    }-*/;

    /**
     * Set a named attribute on the given Node, with special behavior for some names
     * and event handlers. If `value` is `null`, the attribute/handler will be
     * removed.
     * @param node An element to mutate
     * @param name The name/key to set, such as an event or attribute name
     * @param old The last value that was set for this name/node pair
     * @param value An attribute value, such as a function to be used as an
     *  event handler
     * @param isSvg Are we currently diffing inside an svg?
     */
    public static native void setAccessor(JavaScriptObject node, String name, Object old, Object value, boolean isSvg) /*-{

        var IS_NON_DIMENSIONAL = /acit|ex(?:s|g|n|p|$)|rph|ows|mnc|ntw|ine[ch]|zoo|^ord/i;

        if (name==='className') name = 'class';

        if (name==='key') {
            // ignore
        }
        else if (name==='ref') {
            if (old) old(null);
            if (value) value(node);
        }
        else if (name==='class' && !isSvg) {
            node.className = value || '';
        }
        else if (name==='style') {
            if (!value || typeof value==='string' || typeof old==='string') {
                node.style.cssText = value || '';
            }
            if (value && typeof value==='object') {
                if (typeof old!=='string') {
                    for (var i in old) if (!(i in value)) node.style[i] = '';
                }
                for (var i in value) {
                    node.style[i] = typeof value[i]==='number' && IS_NON_DIMENSIONAL.test(i)===false ? (value[i]+'px') : value[i];
                }
            }
        }
        else if (name==='dangerouslySetInnerHTML') {
            if (value) node.innerHTML = value.__html || '';
        }
        else if (name[0]==='o' && name[1]==='n') {
            var useCapture = name !== (name=name.replace(/Capture$/, ''));
            name = name.toLowerCase().substring(2);
            if (value) {
                if (!old) node.addEventListener(name, @Properties::eventProxy(*), useCapture);
            }
            else {
                node.removeEventListener(name, @Properties::eventProxy(*), useCapture);
            }
            (node._listeners || (node._listeners = {}))[name] = value;
        }
        else if (name!=='list' && name!=='type' && !isSvg && name in node) {
            // Attempt to set a DOM property to the given value.
            // IE & FF throw for certain property-value combinations.
            try {
                node[name] = value==null ? '' : value;
            } catch (e) { }
            if ((value==null || value===false) && name!=='spellcheck') node.removeAttribute(name);
        }
        else {
            var ns = isSvg && (name !== (name = name.replace(/^xlink:?/, '')));
            // spellcheck is treated differently than all other boolean values and
            // should not be removed when the value is `false`. See:
            // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#attr-spellcheck
            if (value==null || value===false) {
                if (ns) node.removeAttributeNS('http://www.w3.org/1999/xlink', name.toLowerCase());
                else node.removeAttribute(name);
            }
            else if (typeof value!=='function') {
                if (ns) node.setAttributeNS('http://www.w3.org/1999/xlink', name.toLowerCase(), value);
                else node.setAttribute(name, value);
            }
        }
    }-*/;

    /**
     * Proxy an event to hooked event handlers
     * @param e The event object from the browser
     */
    public static native void eventProxy(JavaScriptObject e) /*-{
        return this._listeners[e.type](e);
    }-*/;
}

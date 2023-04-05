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

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;
import jsinterop.base.Js;
import org.activityinfo.bukavu.client.DomNode;
import org.activityinfo.bukavu.client.RenderQueue;

public abstract class VComponentInstance {

    public DomNode domNode;
    public DomNode nextBase;

    public boolean disabled;
    public VComponentInstance childComponent;
    public VComponentInstance parentComponent;

    /**
     * True if this component needs to be re-rendered on the next pass
     */
    public boolean dirty = false;

    /**
     * Marks this node as dirty
     */
    public final void refresh() {
        RenderQueue.enqueueRender(this);
    }

    /**
     * @return {@code true} if this component can be updated with the given component node.
     */
    public abstract boolean canAcceptProps(VComponent vnode);

    /**
     *
     * Updates this component instance in place with the given properties.
     *
     * @return {@code true} if a re-render is necessary
     *
     */
    public abstract boolean updateProps(VComponentProps props);

    public final boolean isDirty() {
        return dirty;
    }


    /**
     * Renders this component to a concrete {@code VNode}.
     */
    public abstract VNode render();

    /**
     * Called immediately after the component is newly added to the real
     * DOM tree.
     */
    public void componentDidMount() {
    }

    /**
     * Called before the Component is to be unmounted from the dom.
     */
    public void componentWillUnmount() {
    }

    public final Element getDomNode() {
        assert domNode != null : "component has not been mounted";
        return Js.uncheckedCast(domNode);
    }

    public boolean isMounted() {
        return domNode != null;
    }


    public HTMLElement getHTMLElement() {
        return Js.uncheckedCast(domNode);
    }

    public String getKey() {
        return null;
    }


}

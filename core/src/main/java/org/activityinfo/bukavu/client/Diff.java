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

import elemental2.core.JsArray;
import elemental2.core.JsMap;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.NodeList;
import jsinterop.base.Js;
import org.activityinfo.bukavu.shared.tree.*;

import java.util.logging.Logger;


@SuppressWarnings({"checkstyle:cyclomaticcomplexity", "OverlyComplexMethod"})
public class Diff {

    private static final Logger LOGGER = Logger.getLogger(Diff.class.getName());

    private static int diffLevel = 0;

    private static boolean isSvgMode = false;

    private static JsArray<VComponentInstance> mounts = new JsArray<>();

    public static void render(VNode node, Element parent) {
        diff(null, node, Js.uncheckedCast(parent), false);
    }

    /**
     * Apply differences in a given vnode (and it's deep children) to a real DOM Node.
     * @param dom A DOM node to mutate into the shape of a `vnode`
     * @param vnode A VNode (with descendants forming a tree) representing
     *  the desired DOM structure
     * @param parent ?
     * @param componentRoot ?
     * @return The created/mutated element
     */
    public static DomNode diff(DomNode dom, VNode vnode,
                               DomNode parent,
                               boolean componentRoot) {

        // diffLevel having been 0 here indicates initial entry into the diff (not a subdiff)
        if (diffLevel++ == 0) {
            // when first starting the diff, check if we're diffing an SVG or within an SVG
            isSvgMode = parent!=null && parent.getOwnerSVGElement()!=null;
        }

        DomNode ret = idiff(dom, vnode, componentRoot);

        // append the element if its a new parent
        if (parent != null && ret.getParentNode()!=parent) {
            parent.appendChild(ret);
        }

        // diffLevel being reduced to 0 means we're exiting the diff
        if (--diffLevel == 0) {
            // invoke queued componentDidMount lifecycle methods
            if (!componentRoot) {
                flushMounts();
            }
        }

        return ret;
    }

    private static void flushMounts() {
        VComponentInstance c;
        while ((c = mounts.shift()) != null) {
            c.componentDidMount();
        }
    }


    /**
     * Internals of `diff()`, separated to allow bypassing diffLevel / mount flushing.
     *
     * @param dom A DOM node to mutate into the shape of a `vnode`
     * @param vnode A VNode (with descendants forming a tree) representing the desired DOM structure
     * @param componentRoot ?
     */
    private static DomNode idiff(DomNode dom, VNode vnode, boolean componentRoot) {
        DomNode out = dom;
        boolean prevSvgMode = isSvgMode;

        // empty values (null, undefined, booleans) render as empty Text nodes
        if (vnode==null) {
            vnode = VText.EMPTY_TEXT;
        }

        // Fast case: Strings & Numbers create/update Text nodes.
        if (vnode instanceof VText) {

            VText textNode = (VText) vnode;

            // update if it's already a Text node:
            if (dom != null && isTextNode(dom) && dom.getParentNode() != null && (dom.getComponentInstance()==null || componentRoot)) {
                /* istanbul ignore if */ /* Browser quirk that can't be covered: https://github.com/developit/preact/commit/fd4f21f5c45dfd75151bd27b4c217d8003aa5eb9 */
                if (!textNode.getText().equals(dom.getNodeValue())) {
                    dom.setNodeValue(textNode.getText());
                }
            } else {
                // it wasn't a Text node: replace it with one and recycle the old Element
                out = Js.uncheckedCast(DomGlobal.document.createTextNode(textNode.getText()));
                if (dom != null) {
                    if (dom.getParentNode() != null) {
                        dom.getParentNode().replaceChild(out, dom);
                    }
                    recollectNodeTree(dom, true);
                }
            }

            return out;
        }


        // If the VNode represents a Component, perform a component diff:
        if(vnode instanceof VComponent) {
            return buildComponentFromVNode(dom, (VComponent) vnode);
        }
        String vnodeName = ((VElement) vnode).tag;

        // Tracks entering and exiting SVG namespace when descending through the tree.
        if(vnodeName.equals("svg")) {
            isSvgMode = true;
        } else if(isSvgMode && vnodeName.equals("foreignObject")) {
            isSvgMode = false;
        }

        // If there's no existing element or it's the wrong type, create a new one:
        if (dom == null || dom.getComponentInstance() != null || !isNamedNode(dom, vnodeName)) {
            out = createNode(vnodeName, isSvgMode);

            if (dom != null) {
                // move children into the replacement node
                while (dom.getFirstChild() != null) {
                    out.appendChild(dom.getFirstChild());
                }

                // if the previous Element was mounted into the DOM, replace it inline
                if (dom.getParentNode() != null) {
                    dom.getParentNode().replaceChild(out, dom);
                }

                // recycle the old element (skips non-Element node types)
                recollectNodeTree(dom, true);
            }
        }


        DomNode firstChild = out.getFirstChild();

        VAttrMap props = out.getProps();
        VNode[] vchildren = ((VElement) vnode).children;

        if (props==null) {
            props = VAttr.create();
            out.setProps(props);
        }


        // Optimization: fast-path for elements containing a single TextNode:
        if (vchildren != null && vchildren.length==1 &&
                vchildren[0] instanceof VText && firstChild != null && isTextNode(firstChild) && firstChild.getNextSibling() == null) {
            String newText = ((VText) vchildren[0]).getText();
            if (!firstChild.getNodeValue().equals(newText)) {
                firstChild.setNodeValue(newText);
            }

        // otherwise, if there are existing or new children, diff them:
        } else if (vchildren != null && vchildren.length != 0 || firstChild!=null) {
            innerDiffNode(out, vchildren);
        }

        // Apply attributes/props from VNode to the DOM Element
        Properties.diffProperties(out, ((VElement) vnode).properties, props, isSvgMode);


        // restore previous SVG mode: (in case we're exiting an SVG namespace)
        isSvgMode = prevSvgMode;

        return out;
    }

    private static DomNode buildComponentFromVNode(DomNode dom, VComponent vnode) {
        VComponentInstance c = dom == null ? null : dom.getComponentInstance();
        VComponentInstance originalComponent = c;
        DomNode oldDom = dom;
        boolean isDirectOwner = c != null && dom.getComponentInstance().canAcceptProps(vnode);
        boolean isOwner = isDirectOwner;
        VComponentProps props = vnode.getProps();

        while (c != null && !isOwner && (c=c.parentComponent) != null) {
            isOwner = c.canAcceptProps(vnode);
        }

        if (c != null && isOwner && c.childComponent != null) {
            setComponentProps(c, props, RenderMode.ASYNC_RENDER);
            dom = c.domNode;
        } else {
            if (originalComponent != null && !isDirectOwner) {
                unmountComponent(originalComponent);
                dom = oldDom = null;
                c = null;
            }

            if(c == null) {
                c = createComponent(vnode.getConstructor(), props);
            }

            if (dom != null && c.nextBase == null) {
                c.nextBase = dom;
                // passing dom/oldDom as nextBase will recycle it if unused, so bypass recycling on L229:
                oldDom = null;
            }
            setComponentProps(c, props, RenderMode.SYNC_RENDER);
            dom = c.domNode;

            if (oldDom != null && dom!=oldDom) {
                oldDom.setComponentInstance(null);
                Diff.recollectNodeTree(oldDom, false);
            }
        }

        return dom;
    }

    private static VComponentInstance createComponent(VComponentConstructor constructor, VComponentProps props) {
        return constructor.newInstance(props);
    }

        /**
     * Set a component's `props` and possibly re-render the component
     * @param component The Component to set props on
     * @param props The new props
     * @param renderMode Render options - specifies how to re-render the component
     */
    public static void setComponentProps(VComponentInstance component, VComponentProps props, RenderMode renderMode) {
        if (component.disabled) {
            return;
        }
        component.disabled = true;

        component.updateProps(props);

        component.disabled = false;

        if (renderMode!=RenderMode.NO_RENDER) {
            if (renderMode==RenderMode.SYNC_RENDER || component.domNode == null) {
                renderComponent(component, RenderMode.SYNC_RENDER, false);
            } else {
                RenderQueue.enqueueRender(component);
            }
        }
    }


    public static void renderComponent(VComponentInstance component, RenderMode renderMode) {
        renderComponent(component, renderMode, false);
    }

    /**
     * Render a Component, triggering necessary lifecycle events and taking
     * High-Order Components into account.
     * @param component The component to render
     * @param renderMode, see constants.js for available options.
     * @param isChild
     */
    public static void renderComponent(VComponentInstance component, RenderMode renderMode, boolean isChild) {
        if (component.disabled) {
            return;
        }

        boolean isUpdate = component.domNode != null;
        DomNode nextBase = component.nextBase;
        DomNode initialBase = isUpdate ? component.domNode : nextBase;
        VComponentInstance initialChildComponent = component.childComponent;
        VNode rendered;
        VComponentInstance inst = null;
        DomNode cbase;

        component.dirty = false;

        rendered = component.render();

        DomNode base = null;
        VComponentInstance toUnmount = null;

        if (rendered instanceof VComponent) {
            VComponent childComponent = ((VComponent) rendered);

            // set up high order component link

            VComponentProps childProps = childComponent.getProps();
            inst = initialChildComponent;

            if (inst != null && inst.canAcceptProps(childComponent)) {
                setComponentProps(inst, childProps, RenderMode.SYNC_RENDER);
            } else {
                toUnmount = inst;

                component.childComponent = inst = createComponent(childComponent.getConstructor(), childProps);
                inst.nextBase = inst.nextBase != null ? inst.nextBase : nextBase;
                inst.parentComponent = component;
                setComponentProps(inst, childProps, RenderMode.NO_RENDER);
                renderComponent(inst, RenderMode.SYNC_RENDER, true);
            }

            base = inst.domNode;

        } else {
            cbase = initialBase;

            // destroy high order component link
            toUnmount = initialChildComponent;
            if (toUnmount != null) {
                cbase = null;
                component.childComponent = null;
            }

            if (initialBase != null || renderMode==RenderMode.SYNC_RENDER) {
                if (cbase != null) {
                    cbase.setComponentInstance(null);
                }
                base = diff(cbase, rendered, initialBase == null ? null : initialBase.getParentNode(), true);
            }
        }

        if (initialBase != null && base!=initialBase && inst!=initialChildComponent) {
            DomNode baseParent = initialBase.getParentNode();
            if (baseParent != null  && base!=baseParent) {
                baseParent.replaceChild(base, initialBase);

                if (toUnmount == null) {
                    initialBase.setComponentInstance(null);
                    recollectNodeTree(initialBase, false);
                }
            }
        }

        if (toUnmount != null) {
            unmountComponent(toUnmount);
        }

        component.domNode = base;
        if (base != null && !isChild) {
            VComponentInstance componentRef = component;
            VComponentInstance t = component;
            while ((t=t.parentComponent) != null) {
                (componentRef = t).domNode = base;
            }
            base.setComponentInstance(componentRef);
        }

        if (!isUpdate) {
            mounts.push(component);
        }

        if (diffLevel == 0 && !isChild) {
            flushMounts();
        }
    }

    private static boolean isTextNode(DomNode dom) {
        return dom.getSplitText() != null;
    }


    /**
     * Create an element with the given nodeName.
     * @param nodeName The DOM node to create
     * @param isSvg If {@code true}, creates an element within the SVG
     *  namespace.
     * @return  The created DOM node
     */
    public static DomNode createNode(String nodeName, boolean isSvg) {
        DomNode node;
        if (isSvg) {
            node = Js.uncheckedCast(DomGlobal.document.createElementNS("http://www.w3.org/2000/svg", nodeName));
        } else {
            node = Js.uncheckedCast(DomGlobal.document.createElement(nodeName));
        }
        node.setNormalizedNodeName(nodeName);
        return node;
    }


    /**
     * Check if an Element has a given nodeName, case-insensitively.
     * @param node A DOM Element to inspect the name of.
     * @param nodeName Unnormalized name to compare against.
     */
    public static boolean isNamedNode(DomNode node, String nodeName) {
        return nodeName.equals(node.getNormalizedNodeName()) ||
                nodeName.equalsIgnoreCase(node.getNodeName().toLowerCase());
    }

    /**
     * Apply child and attribute changes between a VNode and a DOM Node to the DOM.
     * @param dom Element whose children should be compared & mutated
     * @param vchildren Array of VNodes to compare to `dom.childNodes`
     *  similar to hydration
     */
    public static void innerDiffNode(DomNode dom, VNode[] vchildren) {
        NodeList<DomNode> originalChildren = dom.getChildNodes();
        JsArray<DomNode> children = new JsArray<>();
        JsMap<String, DomNode> keyed = new JsMap<>();

        int keyedLen = 0;
        int min = 0;
        int len = originalChildren.length;
        int childrenLen = 0;
        int vlen = (vchildren == null) ? 0 : vchildren.length;

        // Build up a map of keyed children and an Array of unkeyed children:
        if (len!=0) {
            for (int i=0; i<len; i++) {
                DomNode child = originalChildren.item(i);
                VAttrMap props = child.getProps();
                String key;
                if (vlen != 0 && props != null) {
                    if (child.getComponentInstance() != null) {
                        key = child.getComponentInstance().getKey();
                    } else {
                        key = (String) props.get("key");
                    }
                } else {
                    key = null;
                }
                if (key!=null) {
                    keyedLen++;
                    keyed.set(key, child);
                } else if (props != null || isTextNode(child)) {
                    children.setAt(childrenLen++, child);
                }
            }
        }

        if (vlen!=0) {
            for (int i=0; i<vlen; i++) {
                VNode vchild = vchildren[i];
                DomNode child = null;

                // attempt to find a node based on key matching
                String key = vchild.key;
                if (key!=null) {
                    if (keyedLen != 0 && keyed.has(key)) {
                        child = keyed.get(key);
                        keyed.set(key, null);
                        keyedLen--;
                    }

                // attempt to pluck a node of the same type from the existing children
                } else if (min<childrenLen) {
                    for (int j=min; j<childrenLen; j++) {
                        DomNode c;
                        if (children.getAt(j)!=null && isSameNodeType(c = children.getAt(j), vchild)) {
                            child = c;
                            children.setAt(j, null);
                            if (j==childrenLen-1) {
                                childrenLen--;
                            }
                            if (j==min) {
                                min++;
                            }
                            break;
                        }
                    }
                }

                // morph the matched/found/created DOM child to match vchild (deep)
                child = idiff(child, vchild, false);

                DomNode f = originalChildren.item(i);
                if (child!=null && child!=dom && child!=f) {
                    if (f==null) {
                        dom.appendChild(child);
                    } else if (child==f.getNextSibling()) {
                        removeNode(f);
                    } else {
                        dom.insertBefore(child, f);
                    }
                }
            }
        }


        // remove unused keyed children:
        if (keyedLen != 0) {
            keyed.forEach((value, key, map) -> {
                if(value != null) {
                    recollectNodeTree(value, false);
                }
                return null;
            });
        }

        // remove orphaned unkeyed children:
        while (min<=childrenLen) {
            DomNode child;
            if ((child = children.getAt(childrenLen--))!=null) {
                recollectNodeTree(child, false);
            }
        }
    }

    /**
     * Recursively recycle (or just unmount) a node and its descendants.
     * @param node DOM node to start unmount/removal from
     * @param unmountOnly If {@code true}, only triggers unmount lifecycle, skips removal
     */
    private static void recollectNodeTree(DomNode node, boolean unmountOnly) {
        VComponentInstance component = node.getComponentInstance();
        if (component != null) {
            // if node is owned by a Component, unmount that component (ends up recursing back here)
            unmountComponent(component);
        } else {
            if (!unmountOnly || node.getProps()==null) {
                removeNode(node);
            }
            removeChildren(node);
        }
    }

    /**
     * Remove a component from the DOM and recycle it.
     * @param component The Component instance to unmount
     */
    private static void unmountComponent(VComponentInstance component) {

        DomNode base = component.domNode;

        component.disabled = true;

        component.componentWillUnmount();

        component.domNode = null;

        // recursively tear down & recollect high-order component children:
        VComponentInstance inner = component.childComponent;
        if (inner != null) {
            unmountComponent(inner);

        } else if (base != null) {

            component.nextBase = base;

            removeNode(base);

            removeChildren(base);
        }
    }


    /**
     * Remove a child node from its parent if attached.
     * @param node The node to remove
     */
    public static void removeNode(DomNode node) {
        DomNode parentNode = node.getParentNode();
        if (parentNode != null) {
            parentNode.removeChild(node);
        }
    }


    /**
     * Check if two nodes are equivalent.
     * @param node DOM Node to compare
     * @param vnode Virtual DOM node to compare
     */
    private static boolean isSameNodeType(DomNode node, VNode vnode) {
        if (vnode instanceof VText) {
            return isTextNode(node);
        }
        if (vnode instanceof VElement) {
            return isNamedNode(node, ((VElement) vnode).tag);
        }
        if (vnode instanceof VComponent) {
            return node.getComponentInstance() != null;
        }
        return false;
    }


    /**
     * Recollect/unmount all children.
     *	- we use .lastChild here because it causes less reflow than .firstChild
     *	- it's also cheaper than accessing the .childNodes Live NodeList
     */
    public static void removeChildren(DomNode node) {
        node = node.getLastChild();
        while (node != null) {
            DomNode next = node.getPreviousSibling();
            recollectNodeTree(node, true);
            node = next;
        }
    }

}

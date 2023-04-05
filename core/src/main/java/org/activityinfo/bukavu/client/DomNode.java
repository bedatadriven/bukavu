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

import elemental2.dom.Attr;
import elemental2.dom.Element;
import elemental2.dom.NamedNodeMap;
import elemental2.dom.NodeList;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.activityinfo.bukavu.shared.tree.VAttrMap;
import org.activityinfo.bukavu.shared.tree.VComponentInstance;

@JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
public interface DomNode {

    @JsProperty(name = "ownerSVGElement")
    Element getOwnerSVGElement();

    @JsProperty
    DomNode getParentNode();

    @JsProperty(name = "_component")
    VComponentInstance getComponentInstance();

    @JsProperty(name = "_component")
    void setComponentInstance(VComponentInstance instance);

    @JsProperty
    String getNodeName();

    @JsProperty
    String getNormalizedNodeName();

    @JsProperty
    void setNormalizedNodeName(String nodeName);


    @JsProperty
    String getNodeValue();

    @JsProperty
    void setNodeValue(String text);


    @JsProperty
    DomNode getFirstChild();

    @JsProperty(name = "__preactattr_")
    VAttrMap getProps();

    @JsProperty(name = "__preactattr_")
    void setProps(VAttrMap empty);

    @JsProperty
    NodeList<DomNode> getChildNodes();

    @JsProperty
    DomNode getLastChild();

    @JsProperty
    DomNode getPreviousSibling();

    @JsProperty
    DomNode getNextSibling();

    @JsProperty
    NamedNodeMap<Attr> getAttributes();

    @JsProperty
    Object getSplitText();


    DomNode replaceChild(DomNode newChild, DomNode oldChild);

    DomNode appendChild(DomNode newChild);

    DomNode removeChild(DomNode oldChild);

    DomNode insertBefore(DomNode newChild, DomNode refChild);


}

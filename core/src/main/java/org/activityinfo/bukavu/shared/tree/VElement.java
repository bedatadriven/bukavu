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

import org.activityinfo.bukavu.shared.html.Children;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

public class VElement extends VNode {

    /**
     * Singleton instance for an empty child list.
     *
     */
    public static final VNode[] NO_CHILDREN = new VNode[0];

    public final String tag;
    public final VAttrMap properties;
    public final VNode[] children;

    public VElement(String tag, VNode... children) {
        this(tag, null, children);
    }

    public VElement(String tag, String text) {
        this(tag, null, new VText(text));
    }

    public VElement(String tag, Stream<VNode> children) {
        this(tag, null, children.toArray(VNode[]::new));
    }

    public VElement(String tag, VAttrMap propMap, Stream<VNode> children) {
        this(tag, propMap, children.toArray(VNode[]::new));
    }

    public VElement(String tag, VAttrMap propMap) {
        this(tag, propMap, NO_CHILDREN);
    }

    public VElement(String tag, VAttrMap properties, VNode child) {
        this(tag, properties, new VNode[] { child });
    }

    public VElement(String tag, VAttrMap properties, List<VNode> children) {
        this(tag, properties, Children.toArray(children));
    }

    public VElement(@Nonnull String tag,
                    @Nullable VAttrMap properties,
                    @Nullable VNode... children) {

        this.tag = tag;
        this.properties = properties == null ? VAttrMap.EMPTY : properties;
        this.children = children == null ? NO_CHILDREN : children;
        this.key = (String) this.properties.get("key");

        assert noNullChildren(children);
        assert VAttrMap.EMPTY.isEmpty();
    }

    private static boolean noNullChildren(VNode[] children) {
        for (int i = 0; i < children.length; i++) {
            if(children[i] == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public VNode[] children() {
        return children;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public void accept(VTreeVisitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public String toString() {
        String tag = this.tag.toLowerCase();
        if(children.length == 1 && children[0] instanceof VText) {
            return "<" + tag + ">" + children[0].text() + "</" + tag + "/>";
        } else if(children.length > 0) {
            return "<" + tag + "> ... </" + tag + ">";
        } else {
            return "<" + tag + "/>";
        }
    }
}

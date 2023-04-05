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
package org.activityinfo.bootstrap.client;

import com.google.gwt.safehtml.shared.SafeUri;
import elemental2.dom.DomGlobal;
import elemental2.dom.MouseEvent;
import org.activityinfo.bukavu.shared.html.H;
import org.activityinfo.bukavu.shared.tree.EventHandler;
import org.activityinfo.bukavu.shared.tree.VAttr;
import org.activityinfo.bukavu.shared.tree.VAttrMap;
import org.activityinfo.bukavu.shared.tree.VElement;

public class ButtonBuilder {

    public enum Style {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO,
        LIGHT,
        DARK,
        LINK
    }

    private String label = "";

    private String classSuffix = "primary";

    private boolean outline;

    private VAttrMap attr = VAttr.create();

    private SafeUri link;


    /**
     * Sets the label of this button.
     */
    public ButtonBuilder label(String label) {
        this.label = label;
        return this;
    }

    /**
     * Changes this button to a link with button styling.
     */
    public ButtonBuilder link(SafeUri uri) {
        this.link = uri;
        return this;
    }

    /**
     * Changes the style of this button.
     */
    public ButtonBuilder style(Style style) {
        this.classSuffix = style.name().toLowerCase();
        return this;
    }

    public ButtonBuilder outline(boolean outline) {
        this.outline = outline;
        return this;
    }

    /**
     * Add an event handler for this button being clicked, tapped, or otherwise selected.
     */
    public ButtonBuilder onSelect(EventHandler handler) {
        this.attr.onclick(handler);
        return this;
    }

    public ButtonBuilder onSelect(String confirmationMessage, Runnable runnable) {
        this.attr.onclick(event -> {
            if(DomGlobal.window.confirm(confirmationMessage)) {
                runnable.run();
            }
        });
        return this;
    }

    public ButtonBuilder onMouseUp(EventHandler<MouseEvent> handler) {
        this.attr.on("mouseup", handler);
        return this;
    }
    public VElement build() {
        attr.setClass("btn btn-" + (outline ? "outline-" : "") + classSuffix);
        if(link == null) {
            attr.set("type", "button");
            return new VElement("button", attr, H.t(label));
        } else {
            attr.set("href", link.asString());
            attr.set("role", "button");
            return new VElement("a", attr, H.t(label));
        }
    }
}

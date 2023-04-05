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
package org.activityinfo.bukavu.shared.html;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import org.activityinfo.bukavu.shared.tree.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

import static org.activityinfo.bukavu.shared.html.HtmlTag.*;

public class H {

    public static final String UTF_8_CHARSET = "UTF-8";

    public static final String DEVICE_WIDTH = "device-width";

    public static VElement html(VNode... children) {
        return new VElement(HTML, children);
    }

    public static VElement head(VNode... children) {
        return new VElement(HEAD, children);
    }

    public static VElement meta(VAttrMap propMap) {
        return new VElement(META, propMap);
    }

    public static VElement link(VAttrMap propMap) {
        return new VElement(LINK, propMap);
    }

    public static VAttrMap stylesheet(String href) {
        return VAttr.create().set("rel", "stylesheet").set("href", href);
    }

    public static VElement title(String title) {
        return new VElement(TITLE, t(title));
    }

    public static VElement body(VNode... children) {
        return new VElement(BODY, children);
    }

    private static final VElement EMPTY_DIV = new VElement(DIV);

    public static VElement body(VAttrMap propMap, VNode... children) {
        return new VElement(BODY, propMap, children);
    }

    public static VElement body(@Nonnull Style style, VNode... children) {
        return new VElement(BODY, VAttr.withStyle(style), children);
    }

    public static VElement div(@Nonnull Style style, String text) {
        return new VElement(DIV, VAttr.withStyle(style), new VText(text));
    }

    public static VElement div(VNode... children) {
        return new VElement(DIV, children);
    }

    public static VElement div(String className, Stream<VNode> children) {
        return new VElement(DIV, VAttr.withClass(className), children);
    }

    public static VNode[] nullableList(VNode... array) {
        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if(array[i] != null) {
                count++;
            }
        }
        VNode[] nonEmpty = new VNode[count];
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            if(array[i] != null) {
                nonEmpty[j++] = array[i];
            }
        }
        return nonEmpty;
    }

    public static VNode header(VNode... children) {
        return new VElement(HEADER, children);
    }

    public static VElement div() {
        return EMPTY_DIV;
    }

    public static VElement div(VAttrMap propMap, VNode... children) {
        return new VElement(DIV, propMap, children);
    }

    public static VElement div(String className, VNode... children) {
        return new VElement(DIV, VAttr.withClass(className), children);
    }

    public static VElement table(VAttrMap propMap, VNode... children) {
        return new VElement(TABLE, propMap, children);
    }


    public static VElement table(VNode... children) {
        return new VElement(TABLE, children);
    }


    public static VElement tableHead(VNode... children) {
        return new VElement(THEAD, children);
    }

    public static VElement tableBody(VNode... children) {
        return new VElement(TBODY, children);
    }

    public static VElement tableRow(VNode... children) {
        return new VElement(TR, children);
    }

    public static VElement tableRow(Stream<VNode> children) {
        return new VElement(TR, children);
    }

    public static VElement tableCell(VNode... children) {
        return new VElement(TD, children);
    }

    public static VElement tableCell(String className, VNode... children) {
        return new VElement(TD, VAttr.withClass(className), children);
    }
    public static VElement tableHeaderCell(VNode... children) {
        return new VElement(TH, children);
    }

    public static VElement section(VAttrMap propMap, VNode... children) {
        return new VElement(SECTION, propMap, children);
    }

    public static VElement section(VAttrMap propMap) {
        return new VElement(SECTION, propMap);
    }

    public static VElement section(VNode... children) {
        return new VElement(SECTION, children);
    }

    public static Style style() {
        return new Style();
    }

    public static VAttrMap props() {
        return VAttr.create();
    }

    public static VElement ul() {
        return new VElement(UL);
    }

    public static VElement ul(VNode... children) {
        return new VElement(UL, children);
    }


    public static VElement ul(String className, VNode... children) {
        return new VElement(UL, VAttr.withClass(className), children);
    }

    public static VElement ul(String className, Stream<VNode> children) {
        return new VElement(UL, VAttr.withClass(className), children);
    }
    public static VElement ul(Stream<VNode> children) {
        return new VElement(UL, null, children);
    }

    public static VElement ul(VAttrMap propMap, VNode... children) {
        return new VElement(UL, propMap, children);
    }

    public static VElement li(VNode... children) {
        return new VElement(LI, null, children);
    }
    public static VElement li(VAttrMap propMap, VNode... children) {
        return new VElement(LI, propMap, children);
    }


    public static VElement li(String text) {
        return li(t(text));
    }

    public static VElement link(SafeUri href, VNode... children) {
        return new VElement(A, href(href), children);
    }

    public static VElement a(VAttrMap propMap, VNode... children) {
        return new VElement(A, propMap, children);
    }

    public static VAttrMap href(SafeUri uri) {
        return VAttr.create().href(uri);
    }

    public static VText t(String text) {
        return new VText(text);
    }

    public static VText space() {
        return new VText(" ");
    }

    public static VElement span(String text) {
        return new VElement(SPAN, new VText(text));
    }

    public static VElement span(String classNames, String text) {
        return new VElement(SPAN, VAttr.withClass(classNames), new VText(text));
    }

    public static VNode span(String classNames, VNode... children) {
        return new VElement(SPAN, VAttr.withClass(classNames), children);
    }

    public static VElement span(String classNames, SafeHtml innerHtml) {
        VAttrMap props = VAttr.withClass(classNames);
        props.setInnerHtml(innerHtml);
        return new VElement(SPAN, props);
    }

    public static VElement span(SafeHtml innerHtml) {
        VAttrMap props = VAttr.create();
        props.setInnerHtml(innerHtml);
        return new VElement(SPAN, props);
    }


    public static VElement span(VAttrMap propMap, VNode... children) {
        return new VElement(SPAN, propMap, children);
    }

    public static VElement h1(VNode... children) {
        return new VElement(H1, children);
    }

    public static VElement h1(String text) {
        return new VElement(H1, t(text));
    }

    public static VElement h2(VNode... children) {
        return new VElement(H2, children);
    }

    public static VElement h2(String text) {
        return new VElement(H2, t(text));
    }

    public static VElement h3(VNode... children) {
        return new VElement(H3, children);
    }

    public static VElement h3(VAttrMap propMap, VNode... children) {
        return new VElement(H3, propMap, children);
    }

    public static VElement h3(String text) {
        return new VElement(H3, t(text));
    }

    public static VElement h4(VNode... children) {
        return new VElement(H4, children);
    }

    public static VElement h4(VAttrMap propMap, VNode... children) {
        return new VElement(H4, propMap, children);
    }


    public static VElement h4(String text) {
        return new VElement(H4, t(text));
    }

    public static VElement h5(VNode... children) {
        return new VElement(H5, children);
    }
    public static VElement h5(String text) {
        return new VElement(H5, t(text));
    }

    public static VElement h6(VNode... children) {
        return new VElement(H6, children);
    }

    public static VElement h6(String text) {
        return new VElement(H6, t(text));
    }

    public static VElement p(String text) {
        return new VElement(P, t(text));
    }

    public static VElement p(VNode... children) {
        return new VElement(P, children);
    }

    public static VElement p(VAttrMap propMap, VNode... children) {
        return new VElement(P, propMap, children);
    }

    public static VElement form(VNode... children) {
        return new VElement(FORM, children);
    }

    public static VElement label(VAttrMap propMap, VNode... children) {
        return new VElement(LABEL, propMap, children);
    }

    public static VElement label(String className, VNode... children) {
        return new VElement(LABEL, VAttr.withClass(className), children);
    }

    public static VElement label(VNode... children) {
        return new VElement(LABEL, children);
    }

    public static VAttrMap className(String className) {
        return VAttr.withClass(className);
    }

    public static VElement option(String value, String label) {
        return new VElement(OPTION, VAttr.create().set("value", value), new VText(label));
    }

    public static VElement option(String label) {
        return new VElement(OPTION, new VText(label));
    }

    /**
     * Wraps the text in a Bidirectional Isolate (BDI) tag.
     *
     * <p>This ensures that the direction of any text in the tag is not influenced by the context or
     * the default direction of the document. Should be used for any user-supplied text that might be in
     * a different language than that of the user interface.</p>
     */
    public static VNode bdi(String text) {
        if(text == null || text.isEmpty()) {
            return H.t("");
        } else {
            return new VElement(BDI, H.t(text));
        }
    }

    public static VNode hr() {
        return new VElement(HR);
    }

    public static VNode strong(String text) {
        return new VElement(STRONG, H.t(text));
    }

    public static VNode subtitle(String text) {
        return H.div("subtitle", H.t(text));
    }

    public static VNode surtitle(String text) {
        return H.div("surtitle", H.t(text));
    }

    public interface Render<T> {
        VNode render(T item);
    }

    public static <T> VNode[] map(List<? extends T> items, Render<T> render) {
        VNode[] nodes = new VNode[items.size()];
        for(int i=0;i!=items.size();++i) {
            nodes[i] = render.render(items.get(i));
        }
        return nodes;
    }

    public static VElement script(String src) {
        return new VElement(SCRIPT,
                VAttr.create()
                        .set("language", "javascript")
                .set("src", src));
    }

    public static VAttrMap id(String id) {
        return props().set("id", id);
    }

    public static VElement i(VAttrMap propMap, VNode... children) {
        return new VElement(I, propMap, children);
    }

}

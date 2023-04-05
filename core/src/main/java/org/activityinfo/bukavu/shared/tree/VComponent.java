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

public final class VComponent extends VNode {


    public final VComponentConstructor constructor;
    private final VComponentProps props;

    public VComponent(VComponentConstructor constructor, VComponentProps props) {
        this.constructor = constructor;
        this.props = props;
    }

    public VComponentConstructor getConstructor() {
        return constructor;
    }

    public VComponentProps getProps() {
        return props;
    }

    @Override
    public void accept(VTreeVisitor visitor) {
        visitor.visitComponent(this);
    }

}

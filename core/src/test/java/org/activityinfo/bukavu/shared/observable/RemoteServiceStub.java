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
package org.activityinfo.bukavu.shared.observable;

import java.util.ArrayList;
import java.util.List;

public class RemoteServiceStub {

    private List<RemoteCall> pendingCalls = new ArrayList<>();
    
    public Observable<String> queryName(int id) {
        RemoteCall call = new RemoteCall(id);
        pendingCalls.add(call);
        return call.value;
    }

    public void completePending() {
        for (RemoteCall pendingCall : pendingCalls) {
            pendingCall.complete();
        }
        pendingCalls.clear();
    }


    private static class RemoteCall {
        private int id;
        private ObservableStub<String> value = new ObservableStub<>();

        public RemoteCall(int id) {
            this.id = id;
        }

        public void complete() {
            value.updateValue("name" + id);
        }
    }
    
    
    
}

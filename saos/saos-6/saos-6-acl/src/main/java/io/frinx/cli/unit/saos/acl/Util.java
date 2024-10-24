/*
 * Copyright © 2020 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.saos.acl;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.FORWARDINGACTION;

public final class Util {

    private Util() {
    }

    public static String getFwdActionString(Class<? extends FORWARDINGACTION> action) {
        if (ACCEPT.class.equals(action)) {
            return "allow";
        } else if (DROP.class.equals(action)) {
            return "deny";
        }

        throw new IllegalArgumentException(
                String.format("Possible values are: %s for allow, %s for deny",
                        ACCEPT.class.getSimpleName(), DROP.class.getSimpleName()));
    }

    public static Class<? extends FORWARDINGACTION> getFwdAction(String action) {
        return ("allow".equals(action)) ? ACCEPT.class : DROP.class;
    }
}

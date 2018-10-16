/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.hsrp.handler.util;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;

public abstract class HsrpUtil {

    public static Class<? extends ADDRESSFAMILY> getType(final String type) {
        if ("ipv4".equals(type)) {
            return IPV4.class;
        }

        throw new TypeNotPresentException(type, null);
    }

    public static String getStringType(final Class<? extends ADDRESSFAMILY> type) {
        if (IPV4.class == type) {
            return "ipv4";
        }

        throw new IllegalArgumentException(String.format("accepts types %s", IPV4.class.getSimpleName()));
    }
}

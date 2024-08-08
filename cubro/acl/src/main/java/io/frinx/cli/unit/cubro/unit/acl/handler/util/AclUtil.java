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

package io.frinx.cli.unit.cubro.unit.acl.handler.util;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.cubro.rev200320.ACLIP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;

public abstract class AclUtil {

    public static Class<? extends ACLTYPE> getType(final String type) {
        if ("ipv4".equals(type)) {
            return ACLIPV4.class;
        } else if ("ipv6".equals(type)) {
            return ACLIPV6.class;
        }

        throw new TypeNotPresentException(type, null);
    }

    public static String getName(final Class<? extends ACLTYPE> type) {
        if (ACLIPV4.class.equals(type)) {
            return "ipv4";
        } else if (ACLIPV6.class.equals(type)) {
            return "ipv6";
        }

        throw new TypeNotPresentException(type.getName(), null);
    }

    public static String getStringType(final Class<? extends ACLTYPE> type) {
        if (ACLIPV4.class.equals(type)) {
            return "ip";
        } else if (ACLIPV6.class.equals(type)) {
            return "ipv6";
        }

        throw new IllegalArgumentException(
                String.format("accepts types %s, %s", ACLIPV4.class.getSimpleName(), ACLIPV6.class.getSimpleName())
        );
    }

    public static String chooseIpCommand(Class<? extends ACLTYPE> type) {
        if (type.equals(ACLIP.class)) {
            return "ip";
        }

        throw new IllegalArgumentException("Unsupported acl type: " + type);
    }

    public static String getWildcardfromSubnet(String subnet) {
        String wildcard = "";
        String[] array = subnet.split("\\.");
        for (int index = 0; index < 4; index++) {
            int ii = Integer.parseInt(array[index]);
            ii = StrictMath.abs(ii - 255);
            wildcard = wildcard.concat(ii + ".");
        }
        return wildcard.substring(0, wildcard.length() - 1);
    }
}
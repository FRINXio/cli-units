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

package io.frinx.cli.unit.huawei.acl.handler.util;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4EXTENDED;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.ext.rev180314.ACLIPV4STANDARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;

public abstract class AclUtil {

    public static Class<? extends ACLTYPE> getType(final String type, final String ipv4Type) {
        if ("ip".equalsIgnoreCase(type)) {
            if ("standard".equalsIgnoreCase(ipv4Type)) {
                return ACLIPV4STANDARD.class;
            } else if ("extended".equalsIgnoreCase(ipv4Type)) {
                return ACLIPV4EXTENDED.class;
            }
            return ACLIPV4.class;
        } else if ("ipv6".equalsIgnoreCase(type)) {
            return ACLIPV6.class;
        }

        throw new TypeNotPresentException(type, null);
    }

    public static String getStringType(final Class<? extends ACLTYPE> type) {
        if (AclUtil.isIpv4Acl(type)) {
            return "ip";
        } else if (ACLIPV6.class.equals(type)) {
            return "ipv6";
        }

        throw new IllegalArgumentException(
            String.format("accepts types %s, %s", ACLIPV4.class.getSimpleName(), ACLIPV6.class.getSimpleName())
        );
    }

    public static String chooseIpCommand(Class<? extends ACLTYPE> type) {
        return (ACLIPV6.class.equals(type))
                ? "ipv6 traffic-filter" : "ip access-group";
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

    public static String editAclEntry(final String line, final Class<? extends ACLTYPE> type) {
        // remove match count from line
        String editedLine = line.replaceAll(" \\([^()]*\\)", "");
        if (ACLIPV4STANDARD.class.equals(type)) {
            // remove unnecessary text and double spaces
            editedLine = editedLine.replace(", wildcard bits", "").replaceAll(" +", " ");
            // standard ACL stores host entries as plain IPv4 address, so we should add "host" to the line
            String[] splittedLine = editedLine.split("\\s");
            if (!editedLine.contains("any") && splittedLine.length == 3) {
                editedLine = splittedLine[0] + " " + splittedLine[1] + " host " + splittedLine[2];
            }
        }
        return editedLine;
    }

    public static boolean isIpv4Acl(Class<? extends ACLTYPE> type) {
        return ACLIPV4.class.equals(type) || ACLIPV4STANDARD.class.equals(type) || ACLIPV4EXTENDED.class.equals(type);
    }
}

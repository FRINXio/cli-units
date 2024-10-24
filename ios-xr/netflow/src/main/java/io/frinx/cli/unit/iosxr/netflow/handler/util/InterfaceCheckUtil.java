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

package io.frinx.cli.unit.iosxr.netflow.handler.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class InterfaceCheckUtil {

    // TODO merge check util classes in ACL, ipv6 subinterfaces, and netflow
    static final String CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX = "Parent interface should be ";

    private static boolean checkInterfaceType(final String ifcName, final @NotNull Class... types) {
        final Class<? extends InterfaceType> infType = parseType(ifcName);

        return Lists.newArrayList(types)
                .stream()
                .anyMatch(type -> type.equals(infType));
    }

    public static boolean checkInterfaceType(final InstanceIdentifier<?> iid, final @NotNull Class... types) {
        final Optional<String> interfaceName = getInterfaceName(iid);
        if (interfaceName.isPresent()) {
            return checkInterfaceType(interfaceName.get(), types);
        }

        return false;
    }

    public static void checkInterfaceTypeWithException(final InstanceIdentifier<?> iid, final @NotNull Class... types) {
        final boolean typeIsValid = checkInterfaceType(iid, types);

        Preconditions.checkArgument(typeIsValid,
                CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX + "one of %s",
                types);
    }

    private static Optional<String> getInterfaceName(final InstanceIdentifier<?> iid) {
        final InterfaceKey interfaceKey = iid.firstKeyOf(Interface.class);
        if (interfaceKey == null) {
            return Optional.empty();
        }
        final InterfaceId interfaceId = interfaceKey.getId();
        if (interfaceId == null) {
            return Optional.empty();
        }

        return Optional.of(interfaceId.getValue());
    }

    // TODO extract to util module together with io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader.parseType
    // () in ios-xr-interface-unit
    public static Class<? extends InterfaceType> parseType(String name) {
        if (name.startsWith("FastEther") || name.startsWith("GigabitEthernet") || name.startsWith("TenGigE")) {
            return EthernetCsmacd.class;
        } else if (name.startsWith("Loopback")) {
            return SoftwareLoopback.class;
        } else if (name.startsWith("Bundle-Ether")) {
            return Ieee8023adLag.class;
        } else {
            return Other.class;
        }
    }
}
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
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWMPLS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class NetflowUtils {

    private NetflowUtils() {

    }

    public static Class<? extends NETFLOWTYPE> getType(final String stringType) {
        if ("ipv4".equals(stringType)) {
            return NETFLOWIPV4.class;
        } else if ("ipv6".equals(stringType)) {
            return NETFLOWIPV6.class;
        } else if ("mpls".equals(stringType)) {
            return NETFLOWMPLS.class;
        }

        throw new TypeNotPresentException(stringType, null);
    }

    public static String getNetflowStringType(final Class<? extends NETFLOWTYPE> flowType)
        throws TypeNotPresentException {
        if (flowType.equals(NETFLOWIPV4.class)) {
            return "ipv4";
        } else if (flowType.equals(NETFLOWIPV6.class)) {
            return "ipv6";
        } else if (flowType.equals(NETFLOWMPLS.class)) {
            return "mpls";
        }

        throw new IllegalArgumentException(String.format("Flow type not recognized %s", flowType));
    }

    public static InterfaceKey checkInterfaceExists(final @NotNull InstanceIdentifier<?> iid,
                                                    final @NotNull WriteContext writeContext) {
        final InterfaceKey interfaceKey = iid.firstKeyOf(Interface.class);
        if (interfaceKey == null) {
            throw new IllegalArgumentException(
                    "wrong instance identifier entered, expected child of netflow interface, instanceidentifier: "
                            + iid);
        }
        final Optional<Interface> netflowInterface = writeContext.readAfter(RWUtils.cutId(iid, Interface.class));
        Preconditions.checkArgument(netflowInterface.isPresent(), "netflow cannot be configured because "
                + "interface %s does not exist.", interfaceKey);
        return interfaceKey;
    }
}
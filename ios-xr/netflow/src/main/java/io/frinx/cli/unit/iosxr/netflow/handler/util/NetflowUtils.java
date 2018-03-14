/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.netflow.handler.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWIPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWMPLS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.NETFLOWTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NetflowUtils {

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

    public static InterfaceKey checkInterfaceExists(final @Nonnull InstanceIdentifier<?> iid,
                                             final @Nonnull WriteContext writeContext) {
        final InterfaceKey interfaceKey = iid.firstKeyOf(Interface.class);
        if (interfaceKey == null) {
            throw new IllegalArgumentException(
                "wrong instance identifier entered, expected child of netflow interface, instanceidentifier: " + iid);
        }
        final Optional<Interface> netflowInterface = writeContext.readAfter(RWUtils.cutId(iid, Interface.class));
        Preconditions.checkArgument(netflowInterface.isPresent(), "netflow cannot be configured because " +
            "interface {} does not exist.", interfaceKey);
        return interfaceKey;
    }
}

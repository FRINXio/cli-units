/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

abstract class Ipv6CheckUtil {

    static final String CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX = "Parent interface should be ";
    static final String CHECK_SUBINTERFACE_MSG_PREFIX = "Expected subinterface ";

    static boolean checkParentInterfaceType(final String ifcName, final @Nonnull Class... types) {
        final Class<? extends InterfaceType> infType = InterfaceConfigReader.parseType(ifcName);

        return checkTypes(infType, types);
    }

    static void checkParentInterfaceTypeWithExeption(final String ifcName, final @Nonnull Class... types) {
        final Class<? extends InterfaceType> infType = InterfaceConfigReader.parseType(ifcName);

        final boolean typeIsValid = checkTypes(infType, types);

        Preconditions.checkArgument(typeIsValid,
            CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX + "one of %s",
            Arrays.stream(types).map(Class::getCanonicalName).collect(Collectors.toList()));
    }

    private static boolean checkTypes(final Class<? extends InterfaceType> infType, final @Nonnull Class[] types) {
        return Lists.newArrayList(types).stream().anyMatch(type -> type.equals(infType));
    }

    public static boolean checkSubInterfaceId(final long subIfcIndex, final long expectedSubInterfaceIndex) {
        return subIfcIndex == expectedSubInterfaceIndex;
    }

    public static void checkSubInterfaceIdWithExeption(final long subIfcIndex, final long expectedSubInterfaceIndex) {
        Preconditions.checkArgument(subIfcIndex == expectedSubInterfaceIndex,
            CHECK_SUBINTERFACE_MSG_PREFIX + "is %s", expectedSubInterfaceIndex);
    }
}

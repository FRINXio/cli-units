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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.ifc.Util;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

abstract class Ipv6CheckUtil {

    static final String CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX = "Parent interface should be ";
    static final String CHECK_SUBINTERFACE_MSG_PREFIX = "Expected subinterface ";

    static void checkParentInterfaceTypeWithExeption(final String ifcName, final @NotNull Class... types) {
        final Class<? extends InterfaceType> infType = Util.parseType(ifcName);

        final boolean typeIsValid = checkTypes(infType, types);

        Preconditions.checkArgument(typeIsValid,
                CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX + "one of %s",
                Arrays.stream(types)
                        .map(Class::getCanonicalName)
                        .collect(Collectors.toList()));
    }

    static boolean checkTypes(final Class<? extends InterfaceType> infType, final @NotNull Class... types) {
        return Lists.newArrayList(types)
                .stream()
                .anyMatch(type -> type.equals(infType));
    }

    static void checkSubInterfaceIdWithExeption(final long subIfcIndex, final long expectedSubInterfaceIndex) {
        Preconditions.checkArgument(subIfcIndex == expectedSubInterfaceIndex,
                CHECK_SUBINTERFACE_MSG_PREFIX + "is %s", expectedSubInterfaceIndex);
    }
}
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

package io.frinx.cli.iosxr.unit.acl.handler.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;

public abstract class InterfaceCheckUtil {

    public static void checkInterface(final @Nonnull ReadContext readContext, final String name) {
        // Check interface exists
        final Optional<Interface>
            interfaceOptional = readContext.read(IIDs.INTERFACES.child(
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
            new InterfaceKey(name)
        ));

        checkInterface(interfaceOptional, name);
    }

    public static void checkInterface(final @Nonnull WriteContext writeContext, final String name) {
        // Check interface exists
        final Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface>
            interfaceOptional = writeContext.readAfter(IIDs.INTERFACES.child(
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface.class,
            new InterfaceKey(name)
        ));

        checkInterface(interfaceOptional, name);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static void checkInterface(
        final Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface> interfaceOptional,
        final String name) {
        checkArgument(interfaceOptional.isPresent(), "Interface: %s does not exist, cannot configure ACLs", name);

        checkTypes(interfaceOptional.get().getConfig().getType(),
            EthernetCsmacd.class, Ieee8023adLag.class
        );
    }

    public static void checkTypes(final Class<? extends InterfaceType> infType, final @Nonnull Class... types) {
        Preconditions.checkArgument(Lists.newArrayList(types).stream().anyMatch(type -> type.equals(infType)),
            "Parent interface should be one of %s",
            Arrays.stream(types).map(Class::getSimpleName).collect(Collectors.toList()));
    }
}

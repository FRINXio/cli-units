/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.ifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfInterfaceReader
        implements L3VrfListReader.L3VrfConfigListReader<Interface, InterfaceKey, InterfaceBuilder> {

    private static final String SH_VRF_INTERFACES_TEMPLATE = "show configuration routing-instances %s | display set";
    private static final Pattern VRF_INTERFACE_LINE_PATTERN =
        Pattern.compile("set routing-instances (?<vrfid>\\S+) interface (?<id>\\S+)");

    private final Cli cli;

    public VrfInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIdsForType(
        @Nonnull InstanceIdentifier<Interface> id,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        String vrfName = id.firstKeyOf(NetworkInstance.class).getName();

        if (vrfName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) {
            return Collections.emptyList();
        }

        String output = blockingRead(f(SH_VRF_INTERFACES_TEMPLATE, vrfName), cli, id, ctx);

        return ParsingUtils.parseFields(output, 0,
            VRF_INTERFACE_LINE_PATTERN::matcher,
            m -> m.group("id"),
            InterfaceKey::new);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Interface> list) {
        ((InterfacesBuilder) builder).setInterface(list);
    }

    @Override
    public void readCurrentAttributesForType(
        @Nonnull InstanceIdentifier<Interface> id,
        @Nonnull InterfaceBuilder interfaceBuilder,
        @Nonnull ReadContext ctx) throws ReadFailedException {

        String interfaceId = id.firstKeyOf(Interface.class).getId();
        interfaceBuilder.setId(interfaceId);
    }
}

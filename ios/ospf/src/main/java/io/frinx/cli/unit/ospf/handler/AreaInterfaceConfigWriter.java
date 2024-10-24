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

package io.frinx.cli.unit.ospf.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$config.id}
            ip ospf {$ospf} area {$area}
            {.if ($config.metric) }ip ospf cost {$config.metric.value}
            {/if}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            interface {$config.id}
            no ip ospf {$ospf} area {$area}
            no ip ospf cost
            end""";

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        boolean ifcInVrf = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE))
                .map(NetworkInstance::getInterfaces)
                .map(Interfaces::getInterface)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(i -> i.getId()
                        .equals(data.getId()));

        Preconditions.checkArgument(ifcInVrf, "Interface: %s cannot be in OSPF router: %s, not in the same VRF", data
                .getId(), protocolName);

        blockingWriteAndRead(cli, instanceIdentifier, data,
                fT(WRITE_TEMPLATE,
                        "config", data,
                        "ospf", protocolName,
                        "area", AreaInterfaceReader.areaIdToString(areaId)));

    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        blockingDeleteAndRead(cli, instanceIdentifier,
                fT(DELETE_TEMPLATE,
                        "config", data,
                        "ospf", protocolName,
                        "area", AreaInterfaceReader.areaIdToString(areaId)));
    }
}
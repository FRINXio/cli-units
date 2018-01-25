/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static com.google.common.base.Preconditions.checkArgument;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigWriter implements OspfWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$config.id}\n"
            + "ip ospf {$ospf} area {$area}\n"
            + "{.if ($config.metric) }ip ospf cost {$config.metric.value}\n{/if}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "interface {$config.id}\n"
            + "no ip ospf {$ospf} area {$area}\n"
            + "no ip ospf cost\n"
            + "end";

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class).getIdentifier();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        boolean ifcInVrf = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE))
                .transform(NetworkInstance::getInterfaces)
                .transform(Interfaces::getInterface)
                .or(Collections.emptyList())
                .stream()
                .anyMatch(i -> i.getId().equals(data.getId()));

        checkArgument(ifcInVrf, "Interface: %s cannot be in OSPF router: %s, not in the same VRF", data.getId(), protocolName);

        // TODO ifc has to have IP configured
        // TODO check if ifc not present under different OSPF

        blockingWriteAndRead(cli, instanceIdentifier, data,
                fT(WRITE_TEMPLATE,
                        "config", data,
                        "ospf", protocolName,
                        "area", AreaInterfaceReader.areaIdToString(areaId)));

    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter, WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class).getIdentifier();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingDeleteAndRead(cli, instanceIdentifier,
                fT(DELETE_TEMPLATE,
                        "config", data,
                        "ospf", protocolName,
                        "area", AreaInterfaceReader.areaIdToString(areaId)));
    }
}

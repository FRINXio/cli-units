/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;

public class AreaInterfaceConfigWriter implements OspfWriter<Config> {

    private final Cli cli;
    private static final String WRITE_TEMPLATE = "configure terminal\n"
                                               + "interface %s\n"
                                               + "ip vrf forwarding %s\n"
                                               + "ip ospf %s area %s\n"
                                               + "ip ospf cost %s\n"
                                               + "end";
    private static final String DELETE_TEMPLATE = "configure terminal\n"
                                                + "interface %s\n"
                                                + "no ip vrf forwarding %s\n"
                                                + "no ip ospf %s area %s\n"
                                                + "no ip ospf cost\n"
                                                + "end";

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class).getIdentifier();
        final InterfaceKey intfId = instanceIdentifier.firstKeyOf(Interface.class);

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        // TODO if cost is null, this will result in invalid configuration
        // command 'ip ospf cost null'. Do not even issue the command, if
        // cost is null
        Integer cost = data.getMetric() != null ? data.getMetric().getValue() : null;

        blockingWriteAndRead(cli, instanceIdentifier, data,
            f(WRITE_TEMPLATE,
                intfId.getId(),
                vrfName,
                protocolName, AreaInterfaceReader.areaIdToString(areaId), cost));

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
        final InterfaceKey intfId = instanceIdentifier.firstKeyOf(Interface.class);

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingDeleteAndRead(cli, instanceIdentifier,
            f(DELETE_TEMPLATE,
                intfId.getId(),
                vrfName,
                protocolName, AreaInterfaceReader.areaIdToString(areaId)));
    }
}

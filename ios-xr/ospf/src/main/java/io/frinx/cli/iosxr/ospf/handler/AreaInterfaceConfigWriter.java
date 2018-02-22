/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AreaInterfaceConfigWriter implements OspfWriter<Config> {

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                data.getMetric() != null ? f("cost %s", data.getMetric().getValue()) : "no cost",
                "exit",
                "exit",
                "exit");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config dataBefore, Config dataAfter, WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(instanceIdentifier, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("no interface %s", intfId.getId()),
                "exit",
                "exit");
    }
}

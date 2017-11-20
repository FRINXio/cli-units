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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class AreaInterfaceConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                f("cost %s", data.getMetric().getValue()),
                "commit",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("no interface %s", intfId.getId()),
                "commit",
                "end");
    }
}

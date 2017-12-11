/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.structure.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaConfigWriter implements OspfWriter<Config> {

    private Cli cli;

    public AreaConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", processName),
                f("area %s", AreaInterfaceReader.areaIdToString(data.getIdentifier())),
                "commit",
                "end");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore,
                                               Config dataAfter, WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", processName),
                f("no area %s", AreaInterfaceReader.areaIdToString(data.getIdentifier())),
                "commit",
                "end");
    }
}

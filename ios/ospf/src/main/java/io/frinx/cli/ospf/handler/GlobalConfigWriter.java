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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;

public class GlobalConfigWriter implements OspfWriter<Config> {

    private Cli cli;
    private static final String WRITE_TEMPLATE = "configure terminal\n"
        + "router ospf %s vrf %s\n"
        + "router-id %s\n"
        + "end";
    private static final String DELETE_TEMPLATE = "configure terminal\n"
        + "router ospf %s vrf %s\n"
        + "no router-id %s\n"
        + "end";

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config,
            f(WRITE_TEMPLATE,
                protocolName, vrfName,
                config.getRouterId().getValue()));
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);

    }

    @Override public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String protocolName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        blockingDeleteAndRead(cli, instanceIdentifier,
            f(DELETE_TEMPLATE,
                protocolName, vrfName,
                config.getRouterId().getValue()));
    }
}

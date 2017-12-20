/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;

public class OspfProtocolWriter implements OspfWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
                                               + "router ospf %s vrf %s\n"
                                               + "end";
    private static final String DELETE_TEMPLATE = "configure terminal\n"
                                                + "no router ospf %s vrf %s\n"
                                                + "end";

    private static final String WRITE_TEMPLATE_DEFAULT = "configure terminal\n"
            + "router ospf %s\n"
            + "end";
    private static final String DELETE_TEMPLATE_DEFAULT = "configure terminal\n"
            + "no router ospf %s\n"
            + "end";
    private Cli cli;

    public OspfProtocolWriter(Cli cli) {
        this.cli = cli;
    }

    @Override public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);

        final String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (DEFAULT_NETWORK_NAME.equals(vrfName)) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    f(WRITE_TEMPLATE_DEFAULT, protocolKey.getName()));
        } else {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    f(WRITE_TEMPLATE, protocolKey.getName(), vrfName));
        }
    }

    @Override public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);

        final String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (DEFAULT_NETWORK_NAME.equals(vrfName)) {
            blockingDeleteAndRead(cli, instanceIdentifier, f(DELETE_TEMPLATE_DEFAULT, protocolKey.getName()));
        } else {
            blockingDeleteAndRead(cli, instanceIdentifier, f(DELETE_TEMPLATE, protocolKey.getName(), vrfName));
        }
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}

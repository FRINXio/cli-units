/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class GlobalConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String protName = id.firstKeyOf(Protocol.class).getName();
        String name = (protName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) ? "" : " instance " + protName;
        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("router bgp %s %s", data.getAs().getValue(), name),
                "commit",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String protName = id.firstKeyOf(Protocol.class).getName();
        String name = (protName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) ? "" : " instance " + protName;
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("no router bgp %s %s",  data.getAs().getValue(), name),
                "commit",
                "end");
    }
}

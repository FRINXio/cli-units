/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnel.p2p_top.p2p.tunnel.attributes.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class P2pAttributesConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public P2pAttributesConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (data.getDestination() == null) {
            return;
        }
        final String name = id.firstKeyOf(Tunnel.class).getName();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            f("interface tunnel-te %s", name),
            f("destination %s", data.getDestination().getIpv4Address().getValue()),
            "commit",
            "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class).getName();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            f("interface tunnel-te %s", name),
            f("no destination %s", data.getDestination().getIpv4Address().getValue()),
            "commit",
            "end");
    }
}

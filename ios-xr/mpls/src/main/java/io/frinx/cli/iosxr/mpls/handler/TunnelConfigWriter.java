/*
 * Copyright © 2017 Frinx and others. All rights reserved.
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class TunnelConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public TunnelConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class).getName();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            f("interface tunnel-te %s", name),
            (data.isShortcutEligible()) ? "autoroute announce" : "",
            (data.getMetric() != null && LSPMETRICABSOLUTE.class.equals(data.getMetricType())) ? f("metric absolute %s", data.getMetric()) : "",
            "commit",
            "end");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        this.deleteCurrentAttributes(id, dataBefore, writeContext);
        this.writeCurrentAttributes(id, dataAfter, writeContext);
    }


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class).getName();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            f("no interface tunnel-te %s", name),
            "commit",
            "end");
    }
}

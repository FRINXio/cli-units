/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.network.instance.handler.l3vrf;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public L3VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if(config.getType().equals(L3VRF.class)) {

            blockingWriteAndRead(cli, instanceIdentifier, config,
                    "system-view",
                    f("ip vpn-instance %s", config.getName()),
                    config.getDescription() == null ? "" : f("description %s", config.getDescription()),
                    "ipv4-family",
                    config.getRouteDistinguisher() == null ? ""
                            : f("route-distinguisher %s", config.getRouteDistinguisher().getString()),
                    "commit",
                    "return");
        }
    }

    private static final String DELETE_TEMPLATE = "system-view\n" +
            "undo ip vpn-instance %s\n" +
            "commit\n" +
            "return";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if(config.getType().equals(L3VRF.class)) {

            blockingDeleteAndRead(cli, instanceIdentifier,
                    f(DELETE_TEMPLATE,
                            config.getName()));
        }
    }
}

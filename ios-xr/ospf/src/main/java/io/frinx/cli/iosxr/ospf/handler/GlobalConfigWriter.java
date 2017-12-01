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
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.Config;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class GlobalConfigWriter implements CliWriter<Config>  {

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("router ospf %s", processName),
                "commit",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String processName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                "configure terminal",
                f("no router ospf %s", processName),
                "commit",
                "end");
    }
}

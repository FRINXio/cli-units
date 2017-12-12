/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.snmp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId().getValue();
        if (config.getEnabledTrapForEvent() == null || !LINKUPDOWN.class.equals(config.getEnabledTrapForEvent().get(0).getEventName())) {
            return;
        }
        blockingWriteAndRead(cli, instanceIdentifier, config,
            "configure terminal",
            f("snmp-server interface %s", name),
            "no notification linkupdown disable",
            "commit",
            "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, instanceIdentifier, config,
            "configure terminal",
            f("snmp-server interface %s", name),
            "notification linkupdown disable",
            "commit",
            "end");
    }
}

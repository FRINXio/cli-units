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
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes._interface.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class RsvpInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public RsvpInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            "rsvp",
            f("interface %s", name),
            "commit",
            "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            "rsvp",
            f("no interface %s", name),
            "commit",
            "end");
    }
}

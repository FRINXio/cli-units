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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev171024.NiMplsRsvpIfSubscripAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class NiMplsRsvpIfSubscripAugWriter implements CliWriter<NiMplsRsvpIfSubscripAug> {

    private Cli cli;

    public NiMplsRsvpIfSubscripAugWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> id, @Nonnull NiMplsRsvpIfSubscripAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            "rsvp",
            f("interface %s", name),
            f("bandwidth %s", data.getBandwidth()),
            "commit",
            "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<NiMplsRsvpIfSubscripAug> id, @Nonnull NiMplsRsvpIfSubscripAug data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(cli, id, data,
            "configure terminal",
            "rsvp",
            f("interface %s", name),
            "no bandwidth",
            "commit",
            "end");
    }
}

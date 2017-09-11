/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.essential.crud;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListWriter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.Vrf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.ios.essential.rev170520.vrfs.VrfKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class VrfWriter implements CliListWriter<Vrf, VrfKey> {

    private Cli cli;

    public VrfWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Vrf> id,
                                       @Nonnull Vrf data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("ip vrf %s", data.getId()),
                f("description %s", data.getDescription()),
                "exit",
                "exit");

        // TODO check output
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Vrf> instanceIdentifier,
                                        @Nonnull Vrf before,
                                        @Nonnull Vrf after,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(instanceIdentifier, before, writeContext);
        writeCurrentAttributes(instanceIdentifier, after, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Vrf> id,
                                        @Nonnull Vrf data,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("no ip vrf %s", data.getId()),
                "exit");

        // TODO check output
    }
}

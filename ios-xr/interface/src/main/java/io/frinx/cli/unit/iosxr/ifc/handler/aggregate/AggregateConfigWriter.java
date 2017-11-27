/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AggregateConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AggregateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);
        validateConfig(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                "conf terminal",
                f("interface %s", ifcName),
                f("bundle minimum-active links %s", dataAfter.getMinLinks()),
                "commit",
                "end");
    }

    private static void validateConfig(Config config)  {
        Integer minLinks = config.getMinLinks();

        Preconditions.checkArgument(minLinks == null || minLinks >= 1 && minLinks <= 64,
                "The minimum number of member interfaces %s is not in range of 1 to 64",
                minLinks);
    }

    private static void checkIfcType(String ifcName) {
        Preconditions.checkArgument(AggregateConfigReader.isLAGInterface(ifcName),
                "Cannot configure aggregate config on non LAG interface %s", ifcName);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        checkIfcType(ifcName);

        blockingDeleteAndRead(cli, id,
                "conf terminal",
                f("interface %s", ifcName),
                "no bundle minimum-active links",
                "commit",
                "end");
    }
}

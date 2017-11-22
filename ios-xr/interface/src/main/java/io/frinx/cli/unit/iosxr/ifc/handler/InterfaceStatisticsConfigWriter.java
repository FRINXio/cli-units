/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.io.yang.interfaces.cisco.rev171024.statistics.top.statistics.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceStatisticsConfigWriter implements CliWriter<Config> {
    private Cli cli;

    public InterfaceStatisticsConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        validateConfig(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("interface %s", ifcName),
                f("load-interval %s", dataAfter.getLoadInterval()),
                "commit",
                "end");
    }

    private static void validateConfig(Config dataAfter) {
        Long loadInterval = dataAfter.getLoadInterval();

        // check range
        Preconditions.checkArgument(loadInterval == null || loadInterval >= 0 && loadInterval <= 600,
                "load-interval value %s is not in the range of 0 and 600", loadInterval);

        // check if it is multiple of 30
        Preconditions.checkArgument(loadInterval == null || loadInterval % 30 == 0,
                "load-interval value %s is not multiple of 30", loadInterval);
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

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("interface %s", ifcName),
                "no load-interval",
                "commit",
                "end");
    }
}

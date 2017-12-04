/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public SubinterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        // Only parse configuration for non 0 subifc
        if (subKey.getIndex() == ZERO_SUBINTERFACE_ID) {
            return;
        }

        String subIfcName = getSubinterfaceName(id);
        String cmd = String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName);
        parseInterface(blockingRead(cmd, cli, id, ctx), builder, subKey.getIndex(), subIfcName);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, Long subKey, String name) {
        // Set enabled unless proven otherwise
        builder.setEnabled(true);
        builder.setIndex(subKey);
        builder.setName(name);

        // Actually check if disabled
        parseField(output, 0,
                InterfaceConfigReader.SHUTDOWN_LINE::matcher,
                matcher -> false,
                builder::setEnabled);

        parseField(output,
                InterfaceConfigReader.DESCR_LINE::matcher,
                matcher -> matcher.group("desc"),
                builder::setDescription);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((SubinterfaceBuilder) parentBuilder).setConfig(readValue);
    }
}

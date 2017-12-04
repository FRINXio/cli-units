/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.ethernet;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.EthernetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String AGGREGATE_IFC_NAME = "Bundle-Ether";

    private final Cli cli;

    public EthernetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> id) {
        return new ConfigBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();

        if (!InterfaceConfigWriter.PHYS_IFC_TYPES.contains(InterfaceConfigReader.parseType(ifcName))) {
            // we should parse ethernet configuration just for ethernet interfaces
            return;
        }

        parseEthernetConfig(blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName),
                cli, id, ctx), builder);
    }

    private static final Pattern BUNDLE_ID_LINE = Pattern.compile("\\s*bundle id (?<id>\\d+) mode on\\s*");

    @VisibleForTesting
    static void parseEthernetConfig(String output, ConfigBuilder builder) {

        Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
        ParsingUtils.parseField(output,
                BUNDLE_ID_LINE::matcher,
                matcher -> getLAGInterfaceId(matcher.group("id")),
                ethIfAggregationConfigBuilder::setAggregateId);

        if (ethIfAggregationConfigBuilder.getAggregateId() != null) {
            builder.addAugmentation(Config1.class, ethIfAggregationConfigBuilder.build());
        }
    }

    private static String getLAGInterfaceId(String bundleId) {
        return AGGREGATE_IFC_NAME + bundleId;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((EthernetBuilder) parentBuilder).setConfig(readValue);
    }
}

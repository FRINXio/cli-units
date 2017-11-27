/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.AggregationBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.Config;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.aggregate.rev161222.aggregation.logical.top.aggregation.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AggregateConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public AggregateConfigReader(Cli cli) {
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

        if (!isLAGInterface(ifcName)) {
            // we should read aggregate config just for LAG interfaces
            return;
        }

        parseAggregateConfig(blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName),
                cli, id, ctx), builder);
    }

    private static final Pattern BUNDLE_MINIMUM_LINKS_LINE =
            Pattern.compile("\\s*bundle minimum-active links (?<minLinks>\\d+).*");

    @VisibleForTesting
    static void parseAggregateConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output,
                BUNDLE_MINIMUM_LINKS_LINE::matcher,
                matcher -> Integer.valueOf(matcher.group("minLinks")),
                builder::setMinLinks);
    }

    public static boolean isLAGInterface(String ifcName) {
        return InterfaceConfigReader.parseType(ifcName) == Ieee8023adLag.class;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((AggregationBuilder) parentBuilder).setConfig(readValue);
    }
}

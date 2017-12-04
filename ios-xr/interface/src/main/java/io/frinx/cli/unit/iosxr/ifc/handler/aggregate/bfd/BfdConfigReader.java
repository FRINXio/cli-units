/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.aggregate.bfd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171024.bfd.top.BfdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BfdConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final Logger LOG = LoggerFactory.getLogger(BfdConfigReader.class);

    private final Cli cli;

    public BfdConfigReader(Cli cli) {
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
        if (!AggregateConfigReader.isLAGInterface(ifcName)) {
            // read bfd configuration just for LAG interfaces
            return;
        }

        String output = blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName),
                cli, id, ctx);

        if (!isSupportedBfdConfig(output)) {
            // we support only bfd configuration with mode set to ietf and
            // fast-detect enabled
            LOG.info("{}: The interface {} bfd configuration read from device is not supported."
                    + "'bfd mode ietf' and/or 'bfd address-family ipv4 fast-detect' commands missing", cli, ifcName);
            return;
        }

        parseBfdConfig(output, builder);
    }

    private static final Pattern BFD_MODE_ITEF = Pattern.compile("\\s*bfd mode ietf\\s*");
    private static final Pattern BFD_FAST_DETECT = Pattern.compile("\\s*bfd address-family ipv4 fast-detect\\s*");
    private static final Pattern BFD_MINIMUM_INTERVAL =
            Pattern.compile("\\s*bfd address-family ipv4 minimum-interval (?<minInterval>\\d+)\\s*");
    private static final Pattern BFD_MULTIPLIER =
            Pattern.compile("\\s*bfd address-family ipv4 multiplier (?<multiplier>\\d+)\\s*");
    private static final Pattern BFD_DESTINATION =
            Pattern.compile("\\s*bfd address-family ipv4 destination (?<destination>[\\d.]+)\\s*");

    @VisibleForTesting
    static void parseBfdConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output,
                BFD_MINIMUM_INTERVAL::matcher,
                matcher -> Long.valueOf(matcher.group("minInterval")),
                builder::setMinInterval);

        ParsingUtils.parseField(output,
                BFD_MULTIPLIER::matcher,
                matcher -> Long.valueOf(matcher.group("multiplier")),
                builder::setMultiplier);

        ParsingUtils.parseField(output,
                BFD_DESTINATION::matcher,
                matcher -> new IpAddress(new Ipv4Address(matcher.group("destination"))),
                builder::setDestinationAddress);
    }

    @VisibleForTesting
    static boolean isSupportedBfdConfig(String output) {
        List<Boolean> bfdMode = ParsingUtils.parseFields(output, 0,
                BFD_MODE_ITEF::matcher,
                matcher -> true,
                Function.identity());

        List<Boolean> fastDetect = ParsingUtils.parseFields(output, 0,
                BFD_FAST_DETECT::matcher,
                matcher -> true,
                Function.identity());

        return !fastDetect.isEmpty() && !bfdMode.isEmpty();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((BfdBuilder) parentBuilder).setConfig(readValue);
    }
}

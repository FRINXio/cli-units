/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BfdConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private static final Logger LOG = LoggerFactory.getLogger(BfdConfigReader.class);

    private final Cli cli;

    public BfdConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class)
                .getName();
        if (!new AggregateConfigReader(cli).isLAGInterface(ifcName)) {
            // read bfd configuration just for LAG interfaces
            return;
        }

        String output = blockingRead(String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName),
                cli, id, ctx);

        ConfigBuilder configBuilder = new ConfigBuilder();
        parseBfdConfig(output, configBuilder);
        Config parsedBfdConfig = configBuilder.build();

        if (!isSupportedBfdConfig(output) && !isNotEmpty(parsedBfdConfig)) {
            // we support only bfd configuration with mode set to ietf and
            // fast-detect enabled
            LOG.info("{}: The interface {} bfd configuration read from device is not supported."
                    + "'bfd mode ietf' and/or 'bfd address-family ipv4 fast-detect' commands missing", cli, ifcName);
            return;
        }

        builder.fieldsFrom(parsedBfdConfig);
    }

    private static boolean isNotEmpty(Config parsedBfdConfig) {
        return parsedBfdConfig.getDestinationAddress() != null || parsedBfdConfig.getMinInterval() != null
                || parsedBfdConfig.getMultiplier() != null;
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
            matcher -> new Ipv4Address(matcher.group("destination")),
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
}
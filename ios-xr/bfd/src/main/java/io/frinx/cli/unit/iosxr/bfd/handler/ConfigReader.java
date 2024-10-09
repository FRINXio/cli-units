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

package io.frinx.cli.unit.iosxr.bfd.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.IfBfdExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.top.bfd.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_INTERFACE_CFG = "show running-config interface %s";
    private static final Pattern BFD_MINIMUM_INTERVAL =
            Pattern.compile("\\s*bfd address-family ipv4 minimum-interval (?<minInterval>\\d+)\\s*");
    private static final Pattern BFD_MULTIPLIER =
            Pattern.compile("\\s*bfd address-family ipv4 multiplier (?<multiplier>\\d+)\\s*");
    private static final Pattern BFD_DESTINATION =
            Pattern.compile("\\s*bfd address-family ipv4 destination (?<destination>[\\d.]+)\\s*");

    private final Cli cli;

    public ConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder, @NotNull ReadContext readContext)
            throws ReadFailedException {
        final String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getId();
        final String output = blockingRead(
                String.format(SH_SINGLE_INTERFACE_CFG, ifcName),
                cli,
                instanceIdentifier,
                readContext);
        parseBfdConfig(output, ifcName, configBuilder);
    }

    static void parseBfdConfig(@NotNull String output, @NotNull String ifcName, ConfigBuilder configBuilder) {
        configBuilder.setId(ifcName);

        ParsingUtils.parseField(output,
                BFD_MINIMUM_INTERVAL::matcher,
            matcher -> Long.valueOf(matcher.group("minInterval")),
                configBuilder::setDesiredMinimumTxInterval);

        ParsingUtils.parseField(output,
                BFD_MULTIPLIER::matcher,
            matcher -> Integer.valueOf(matcher.group("multiplier")),
                configBuilder::setDetectionMultiplier);

        final IfBfdExtAugBuilder remoteAddressAugmentationBuilder = new IfBfdExtAugBuilder();
        ParsingUtils.parseField(output,
                BFD_DESTINATION::matcher,
            matcher -> new IpAddress(new Ipv4Address(matcher.group("destination"))),
                remoteAddressAugmentationBuilder::setRemoteAddress);
        configBuilder.addAugmentation(IfBfdExtAug.class, remoteAddressAugmentationBuilder.build());
    }
}
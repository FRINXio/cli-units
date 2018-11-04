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

package io.frinx.cli.unit.iosxr.lacp.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.lacp.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BundleConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public BundleConfigReader(Cli cli) {
        this.cli = cli;
    }

    private static final Pattern LACP_MODE_LINE = Pattern.compile("\\s*lacp mode (?<mode>(active|passive)).*");

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext)
            throws ReadFailedException {
        final String bundleName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        final String searchedInterface = String.format(MemberConfigReader.SH_SINGLE_INTERFACE, bundleName);
        final String interfaceConfiguration = blockingRead(searchedInterface, cli, instanceIdentifier, readContext);
        readConfiguration(bundleName, interfaceConfiguration, configBuilder);
    }

    static void readConfiguration(@Nonnull String bundleName, @Nonnull String interfaceConfiguration,
                           ConfigBuilder configBuilder) {
        configBuilder.setName(bundleName);
        configBuilder.setLacpMode(parseLacpMode(interfaceConfiguration));
        configBuilder.setInterval(MemberConfigReader.parseLacpPeriodType(interfaceConfiguration).get());
    }

    private static LacpActivityType parseLacpMode(@Nonnull String interfaceConfiguration) {
        return ParsingUtils.parseField(
                interfaceConfiguration,
                0,
                LACP_MODE_LINE::matcher,
            matcher -> matcher.group("mode"))
                .map(mode -> LacpActivityType.valueOf(mode.toUpperCase()))
                .orElse(null);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((InterfaceBuilder) builder).setConfig(config);
    }
}

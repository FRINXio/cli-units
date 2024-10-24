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

package io.frinx.cli.unit.nexus.ifc.handler.subifc.ipv6;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.router.advertisement.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv6AdvertisementConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_SINGLE_SUB_INTERFACE_CFG = "show running-config interface %s";
    private static final Pattern IPV6_ADVERTISEMENT_ENABLED = Pattern.compile("\\s*ipv6 nd suppress-ra.*");

    private final Cli cli;

    public Ipv6AdvertisementConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder configBuilder,
                                      @NotNull final ReadContext readContext)
            throws ReadFailedException {

        String ifcName = id.firstKeyOf(Interface.class)
                .getName();

        parseAdvertisementConfig(
                blockingRead(String.format(SH_SINGLE_SUB_INTERFACE_CFG, ifcName), cli, id, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseAdvertisementConfig(final String output, final ConfigBuilder builder) {
        // ipv6 nd suppress-ra
        ParsingUtils.parseField(output, 0,
            IPV6_ADVERTISEMENT_ENABLED::matcher,
            matcher -> true,
            builder::setSuppress);
    }
}
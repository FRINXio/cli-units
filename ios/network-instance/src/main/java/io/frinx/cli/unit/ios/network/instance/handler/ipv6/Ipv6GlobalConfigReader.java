/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.network.instance.handler.ipv6;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.ipvsix.cisco.rev210630.cisco.ipv6.global.config.CiscoIpv6ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class Ipv6GlobalConfigReader implements CliConfigReader<CiscoIpv6Config, CiscoIpv6ConfigBuilder> {

    private static final String SH_IPV6_CONFIG = "show running-config | include ^ipv6";
    private static final String SH_CEF_IPV6_INFO = "show ipv6 cef summary";

    private final Cli cli;

    public Ipv6GlobalConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<CiscoIpv6Config> instanceIdentifier,
                                      @Nonnull CiscoIpv6ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (!NetworInstance.DEFAULT_NETWORK.equals(instanceIdentifier.firstKeyOf(NetworkInstance.class))) {
            return;
        }
        String unicastRoutingOutput = blockingRead(SH_IPV6_CONFIG, cli, instanceIdentifier, readContext);
        String cefOutput = blockingRead(SH_CEF_IPV6_INFO, cli, instanceIdentifier, readContext);
        parseIpv6Config(configBuilder, unicastRoutingOutput, cefOutput);
    }

    @VisibleForTesting
    static void parseIpv6Config(CiscoIpv6ConfigBuilder configBuilder, String unicastRoutingOutput, String cefOutput) {
        configBuilder
            .setCefEnabled(ParsingUtils.NEWLINE.splitAsStream(cefOutput)
                .anyMatch(m -> m.contains("IPv6 CEF is enabled and running centrally")))
            .setUnicastRoutingEnabled(ParsingUtils.NEWLINE.splitAsStream(unicastRoutingOutput)
                .anyMatch(m -> m.contains("unicast-routing")))
            .build();
    }
}

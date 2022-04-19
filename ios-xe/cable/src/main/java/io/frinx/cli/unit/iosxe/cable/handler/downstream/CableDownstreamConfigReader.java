/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable.handler.downstream;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.downstream.cable.profile.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.downstream.cable.profile.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableDownstreamConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_CABLE_DOWN = "show running-config | section cable downstream controller-profile %s";
    private static final Pattern MAX_CARRIER_LINE = Pattern.compile(".*max-carrier (?<carrier>\\d+)");
    private static final Pattern MAX_OFDM_LINE = Pattern.compile(".*max-ofdm-spectrum (?<ofdm>\\d+)");

    private final Cli cli;

    public CableDownstreamConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String downId = instanceIdentifier.firstKeyOf(DownstreamCableProfile.class).getId();
        final String rpdOutput = blockingRead(f(SH_CABLE_DOWN, downId),
                cli, instanceIdentifier, readContext);

        parseConfig(rpdOutput, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, MAX_CARRIER_LINE::matcher,
            matcher -> matcher.group("carrier"),
            configBuilder::setMaxCarrier);

        ParsingUtils.parseField(output, MAX_OFDM_LINE::matcher,
            matcher -> matcher.group("ofdm"),
            configBuilder::setMaxOfdmSpectrum);
    }

}
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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.ofdm.top.ofdm.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.ofdm.top.ofdm.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RfChannelOfdmConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final Pattern OFDM_LINE = Pattern.compile("ofdm channel-profile (?<cp>\\d+)"
            + " start-frequency (?<frequency>\\d+) width (?<width>\\d+) plc (?<plc>\\d+)");

    private final Cli cli;

    public RfChannelOfdmConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String downId = instanceIdentifier.firstKeyOf(DownstreamCableProfile.class).getId();
        final String rfChannelId = instanceIdentifier.firstKeyOf(RfChannel.class).getId();
        final String rpdOutput = blockingRead(f(CableDownstreamConfigReader.SH_CABLE_DOWN, downId),
                cli, instanceIdentifier, readContext);
        parseConfig(rpdOutput, configBuilder, rfChannelId);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder, String name) {
        Pattern.compile("\\n\\s\\S").splitAsStream(output)
            .filter(value -> value.contains("chan " + name))
            .findFirst()
            .ifPresent(s -> {
                ParsingUtils.parseField(s, OFDM_LINE::matcher,
                    matcher -> matcher.group("cp"),
                    configBuilder::setChannelProfile);

                ParsingUtils.parseField(s, OFDM_LINE::matcher,
                    matcher -> matcher.group("frequency"),
                    configBuilder::setStartFrequency);

                ParsingUtils.parseField(s, OFDM_LINE::matcher,
                    matcher -> matcher.group("width"),
                    configBuilder::setWidth);

                ParsingUtils.parseField(s, OFDM_LINE::matcher,
                    matcher -> matcher.group("plc"),
                    configBuilder::setPlc);
            });
    }
}
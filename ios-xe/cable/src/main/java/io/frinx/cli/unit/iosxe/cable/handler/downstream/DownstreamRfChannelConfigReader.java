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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.RfChanConfig.RfOutput;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DownstreamRfChannelConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final Pattern FREQUENCY_LINE = Pattern.compile(".*frequency (?<id>\\d+)");
    static final Pattern QAM_LINE = Pattern.compile(".*qam-profile (?<id>\\d+)");
    static final Pattern DOCSIS_LINE = Pattern.compile(".*docsis-channel-id (?<id>\\d+)");
    static final Pattern RF_LINE = Pattern.compile(".*rf-output (?<id>.+)");

    private final Cli cli;

    public DownstreamRfChannelConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
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
                ParsingUtils.parseField(s, FREQUENCY_LINE::matcher,
                    matcher -> matcher.group("id"),
                    configBuilder::setFrequency);

                ParsingUtils.parseField(s, DOCSIS_LINE::matcher,
                    matcher -> matcher.group("id"),
                    configBuilder::setDocsisChannelId);

                ParsingUtils.parseField(s, QAM_LINE::matcher,
                    matcher -> matcher.group("id"),
                    configBuilder::setQamProfile);

                ParsingUtils.parseField(s, RF_LINE::matcher,
                    matcher -> matcher.group("id"),
                    setter -> configBuilder.setRfOutput(getRfOutputValue(setter)));
            });
    }

    private static RfOutput getRfOutputValue(final String name) {
        for (final RfOutput rfOutput : RfOutput.values()) {
            if (name.equalsIgnoreCase(rfOutput.getName())) {
                return rfOutput;
            }
        }
        return null;
    }

}
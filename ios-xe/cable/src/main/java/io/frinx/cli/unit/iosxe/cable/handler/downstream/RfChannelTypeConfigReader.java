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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.Config.Mode;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.Config.RfType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.rf.chan.type.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RfChannelTypeConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final Pattern TYPE_LINE = Pattern.compile(".*type (?<type>\\S+).*");
    static final Pattern MODE_LINE = Pattern.compile("type ([A-Z]+) (?<mode>\\S+).*");

    private final Cli cli;

    public RfChannelTypeConfigReader(Cli cli) {
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
                ParsingUtils.parseField(s, TYPE_LINE::matcher,
                    matcher -> matcher.group("type"),
                    setter -> configBuilder.setRfType(getType(setter)));
                ParsingUtils.parseField(s, MODE_LINE::matcher,
                    matcher -> matcher.group("mode"),
                    setter -> configBuilder.setMode(getMode(setter)));
            });
    }

    private static Mode getMode(final String name) {
        for (final Mode mode : Mode.values()) {
            if (name.equalsIgnoreCase(mode.getName())) {
                return mode;
            }
        }
        return null;
    }

    private static RfType getType(final String name) {
        for (final RfType type : RfType.values()) {
            if (name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }
}

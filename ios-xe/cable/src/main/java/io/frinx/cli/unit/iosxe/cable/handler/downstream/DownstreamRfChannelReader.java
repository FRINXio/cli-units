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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.downstream.top.downstreams.DownstreamCableProfile;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.RfChannelKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rf.chan.top.rf.channels.rf.channel.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DownstreamRfChannelReader implements CliConfigListReader<RfChannel, RfChannelKey, RfChannelBuilder> {

    static final Pattern RF_CHAN_LINE = Pattern.compile("rf-chan (?<id>.+)");

    private final Cli cli;

    public DownstreamRfChannelReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<RfChannelKey> getAllIds(@Nonnull InstanceIdentifier<RfChannel> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        final String downId = instanceIdentifier.firstKeyOf(DownstreamCableProfile.class).getId();
        final String output = blockingRead(f(CableDownstreamConfigReader.SH_CABLE_DOWN, downId),
                cli, instanceIdentifier, readContext);

        return getRfChanKeys(output);
    }

    @VisibleForTesting
    static List<RfChannelKey> getRfChanKeys(String output) {
        return ParsingUtils.parseFields(output, 0, RF_CHAN_LINE::matcher,
            matcher -> matcher.group("id"), RfChannelKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<RfChannel> instanceIdentifier,
                                      @Nonnull RfChannelBuilder rfChannelBuilder,
                                      @Nonnull ReadContext readContext) {
        final String rfChanId = instanceIdentifier.firstKeyOf(RfChannel.class).getId();
        rfChannelBuilder.setId(rfChanId);
        rfChannelBuilder.setConfig(new ConfigBuilder().setId(rfChanId).build());
    }
}
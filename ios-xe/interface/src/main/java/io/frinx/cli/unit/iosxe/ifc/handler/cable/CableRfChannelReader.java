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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannels;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.cable.top.cable.RfChannelsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class CableRfChannelReader implements CliConfigReader<RfChannels, RfChannelsBuilder> {

    private static final Pattern CABLE_RF_CHANNEL_LINE =
            Pattern.compile("cable rf-channels channel-list (?<list>.+) bandwidth-percent (?<percent>.+)");

    private final Cli cli;

    public CableRfChannelReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<RfChannels> id,
                                      @Nonnull RfChannelsBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final String showRunIfcConfig = f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName);
        parseCable(blockingRead(showRunIfcConfig, cli, id, ctx), builder);
    }

    @VisibleForTesting
    static void parseCable(final String output, final RfChannelsBuilder builder) {
        ParsingUtils.parseField(output,
            CABLE_RF_CHANNEL_LINE::matcher,
            matcher -> matcher.group("list"),
            builder::setChannelList);

        ParsingUtils.parseField(output,
            CABLE_RF_CHANNEL_LINE::matcher,
            matcher -> matcher.group("percent"),
            builder::setBandwidthPercent);
    }
}

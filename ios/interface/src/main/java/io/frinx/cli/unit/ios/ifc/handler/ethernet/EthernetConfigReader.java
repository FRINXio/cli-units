/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.ifc.handler.ethernet;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.Util;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.ext.rev190724.SPEEDAUTO;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.lag.member.rev171109.LacpEthConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.lacp.rev170505.LacpActivityType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern MEDIA_TYPE_LINE = Pattern.compile("\\s*media-type (?<mediaType>.+)");
    public static final Pattern PORT_SPEED_LINE = Pattern.compile("\\s*speed (?<portSpeed>.+)");
    private static final Pattern CHANNEL_GROUP_ID_LINE = Pattern.compile("\\s*channel-group (?<id>\\d+).*");
    private static final Pattern LACP_MODE_LINE = Pattern.compile("\\s*channel-group (?<id>\\d+) mode (?<mode>.+)");

    private final Cli cli;

    public EthernetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        final String ifcOutput = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx);
        parseEthernetConfig(ifcName, ifcOutput, builder);
    }

    @VisibleForTesting
    static void parseEthernetConfig(String ifcName, String ifcOutput, ConfigBuilder builder) {
        if (canSetSpeed(ifcName, ifcOutput)) {
            setPortSpeed(ifcOutput, builder);
        }

        if (Util.isPhysicalInterface(Util.parseType(ifcName))) {
            Config1Builder ethIfAggregationConfigBuilder = new Config1Builder();
            setAggregationId(ifcOutput, ethIfAggregationConfigBuilder);

            if (ethIfAggregationConfigBuilder.getAggregateId() != null) {
                builder.addAugmentation(Config1.class, ethIfAggregationConfigBuilder.build());
                LacpEthConfigAugBuilder ethConfigAugBuilder = new LacpEthConfigAugBuilder();
                setLacpMode(ifcOutput, ethConfigAugBuilder);
                if (ethConfigAugBuilder.getLacpMode() != null) {
                    builder.addAugmentation(LacpEthConfigAug.class, ethConfigAugBuilder.build());
                }
            }
        }
    }

    private static boolean canSetSpeed(String ifcName, String ifcOutput) {
        // speed can only be set when media-type is set
        return Util.canSetInterfaceSpeed(ifcName) && MEDIA_TYPE_LINE.matcher(ifcOutput).find();
    }

    private static void setLacpMode(String ifcOutput, LacpEthConfigAugBuilder ethConfigAugBuilder) {
        ParsingUtils.parseField(ifcOutput,
            LACP_MODE_LINE::matcher,
            matcher -> matcher.group("mode"),
            mode -> ethConfigAugBuilder.setLacpMode(LacpActivityType.valueOf(mode.toUpperCase())));
    }

    private static void setAggregationId(String ifcOutput, Config1Builder ethIfAggregationConfigBuilder) {
        ParsingUtils.parseField(ifcOutput,
            CHANNEL_GROUP_ID_LINE::matcher,
            matcher -> matcher.group("id"),
            ethIfAggregationConfigBuilder::setAggregateId);
    }

    private static void setPortSpeed(String ifcOutput, ConfigBuilder builder) {
        Optional<String> speedValue = ParsingUtils.parseField(ifcOutput, 0,
            PORT_SPEED_LINE::matcher,
            matcher -> matcher.group("portSpeed"));

        if (speedValue.isPresent()) {
            builder.setPortSpeed(Util.parseSpeed(speedValue.get()));
        } else {
            builder.setPortSpeed(SPEEDAUTO.class);
        }
    }

}

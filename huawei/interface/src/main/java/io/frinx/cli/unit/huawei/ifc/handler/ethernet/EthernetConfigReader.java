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

package io.frinx.cli.unit.huawei.ifc.handler.ethernet;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.Util;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.aggregate.rev161222.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ethernet.rev161222.ethernet.top.ethernet.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EthernetConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern ETH_TRUNK_LINE = Pattern.compile("\\s*eth-trunk (?<trunk>\\d+).*");

    private final Cli cli;

    public EthernetConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        final var ifcName = id.firstKeyOf(Interface.class).getName();
        final var ifcOutput = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx);
        parseEthernetConfig(ifcName, ifcOutput, builder);
    }

    @VisibleForTesting
    static void parseEthernetConfig(String ifcName, String ifcOutput, ConfigBuilder builder) {
        if (Util.isPhysicalInterface(Util.parseType(ifcName))) {
            var ethIfAggrConfigBuilder = new Config1Builder();
            setAggregationId(ifcOutput, ethIfAggrConfigBuilder);

            if (ethIfAggrConfigBuilder.getAggregateId() != null) {
                builder.addAugmentation(Config1.class, ethIfAggrConfigBuilder.build());
            }
        }
    }

    private static void setAggregationId(String ifcOutput, Config1Builder ethIfAggrConfigBuilder) {
        ParsingUtils.parseField(ifcOutput,
                ETH_TRUNK_LINE::matcher,
                matcher -> matcher.group("trunk"),
                ethIfAggrConfigBuilder::setAggregateId);
    }
}
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

package io.frinx.cli.unit.iosxr.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_OSPF_INT = "show running-config router ospf %s %s area %s interface %s";
    private static final Pattern COST_LINE = Pattern.compile("cost (?<cost>.+)");
    private static final Pattern PASSIVE_LINE = Pattern.compile("passive (?<passive>.+)");
    private final Cli cli;

    public AreaInterfaceConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull
            ConfigBuilder configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        final String ospfId = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
        configBuilder.setId(key.getId());
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SHOW_OSPF_INT, ospfId, nwInsName, areaId, key.getId()), cli,
                instanceIdentifier, readContext);
        parseCost(output, configBuilder);
        parsePassive(output, configBuilder);
    }

    @VisibleForTesting
    public static void parseCost(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
                COST_LINE::matcher,
            matcher -> matcher.group("cost"),
            value -> configBuilder.setMetric(new OspfMetric(Integer.parseInt(value))));
    }

    @VisibleForTesting
    public static void parsePassive(String output, ConfigBuilder configBuilder) {

        ParsingUtils.NEWLINE.splitAsStream(output)
            .map(PASSIVE_LINE::matcher)
            .filter(Matcher::find)
            .findAny()
            .ifPresent(matcher -> {
                if ("disable".equals(matcher.group("passive"))) {
                    configBuilder.setPassive(false);
                } else {
                    configBuilder.setPassive(true);
                }
            });
    }
}
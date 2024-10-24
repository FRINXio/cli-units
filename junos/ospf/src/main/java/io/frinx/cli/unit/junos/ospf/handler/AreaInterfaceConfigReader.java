/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.OspfAreaIfConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.OspfAreaIfConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OSPFNETWORKTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.POINTTOPOINTNETWORK;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @VisibleForTesting
    static final String SHOW_OSPF_INT =
            "show configuration%s protocols ospf area %s interface %s | display set";
    private static final Pattern ENABLED_LINE =
            Pattern.compile("set.* protocols ospf area \\S+ interface \\S+ disable");
    private static final Pattern NETWORK_TYPE_LINE =
            Pattern.compile("set.* protocols ospf area \\S+ interface \\S+ interface-type (?<type>.+)");
    private static final Pattern COST_LINE =
            Pattern.compile("set.* protocols ospf area \\S+ interface \\S+ metric (?<metric>.+)");
    private static final Pattern PRIORITY_LINE =
            Pattern.compile("set.* protocols ospf area \\S+ interface \\S+ priority (?<priority>.+)");
    private final Cli cli;

    public AreaInterfaceConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull
            ConfigBuilder configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final InterfaceKey key = instanceIdentifier.firstKeyOf(Interface.class);
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier());
        configBuilder.setId(key.getId());
        final String nwInsName = OspfProtocolReader.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SHOW_OSPF_INT, nwInsName, areaId, key.getId()), cli,
                instanceIdentifier, readContext);
        parseEnabled(output, configBuilder);
        parseNetworkType(output, configBuilder);
        parseMetric(output, configBuilder);
        parsePriority(output, configBuilder);
    }

    private static void parseEnabled(String output, ConfigBuilder configBuilder) {
        OspfAreaIfConfAugBuilder builder = new OspfAreaIfConfAugBuilder();
        builder.setEnabled(true);

        ParsingUtils.parseField(output,
            ENABLED_LINE::matcher,
            matcher -> false,
            value -> builder.setEnabled(false));
        configBuilder.addAugmentation(OspfAreaIfConfAug.class, builder.build());
    }

    private static void parseNetworkType(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            NETWORK_TYPE_LINE::matcher,
            matcher -> matcher.group("type"),
            value -> configBuilder.setNetworkType(parseNwType(value)));
    }

    private static void parseMetric(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            COST_LINE::matcher,
            matcher -> matcher.group("metric"),
            value -> configBuilder.setMetric(new OspfMetric(Integer.parseInt(value))));
    }

    private static Class<? extends OSPFNETWORKTYPE> parseNwType(final String name) {
        if (name.equals("p2p")) {
            return POINTTOPOINTNETWORK.class;
        } else {
            return null;
        }
    }

    private static void parsePriority(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output,
            PRIORITY_LINE::matcher,
            matcher -> matcher.group("priority"),
            value -> configBuilder.setPriority(Short.parseShort(value)));
    }
}
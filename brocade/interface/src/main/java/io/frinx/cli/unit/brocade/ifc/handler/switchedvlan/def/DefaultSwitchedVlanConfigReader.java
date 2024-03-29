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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultSwitchedVlanConfigReader implements CompositeReader.Child<Config, ConfigBuilder>,
        CliConfigReader<Config, ConfigBuilder> {

    public static final String SWITCHED_VLAN_CONFIG = "show running-config vlan | include vlan | *tagged | router*";

    private static final Pattern IFC = Pattern.compile("e(the|thernet)? (?<interface>\\d/\\d+)");
    public static final Pattern NEWLINE = Pattern.compile(" \n ");
    public static final String VLAN_KEY = "VLAN_KEY";

    private final Cli cli;

    public DefaultSwitchedVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        Matcher ifMatcher = IFC.matcher(instanceIdentifier.firstKeyOf(Interface.class).getName());
        if (ifMatcher.matches()) {
            parseVlanConfig(blockingRead(SWITCHED_VLAN_CONFIG, cli, instanceIdentifier, readContext),
                    configBuilder, ifMatcher.group("interface"), readContext);
        }
    }

    @VisibleForTesting
    static void parseVlanConfig(String output, ConfigBuilder configBuilder, String ifcName, ReadContext readContext) {
        List<Vlan> vlans = (List<Vlan>) readContext.getModificationCache().get(VLAN_KEY);
        if (vlans == null) {
            readContext.getModificationCache().put(VLAN_KEY, vlans = ParsingUtils.NEWLINE
                    .splitAsStream(NEWLINE.matcher(output).replaceAll(" "))
                    .map(Vlan::create)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        vlans.forEach(v -> Vlan.buildConfiguration(ifcName, configBuilder, v));

        configBuilder.setTrunkVlans(Vlan.optimizeTrunkVlans(configBuilder.getTrunkVlans()));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
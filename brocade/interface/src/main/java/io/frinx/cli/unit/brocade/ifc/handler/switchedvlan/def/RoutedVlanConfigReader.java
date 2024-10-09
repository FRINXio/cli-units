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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.routed.top.routed.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.routed.top.routed.vlan.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class RoutedVlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern ROUTER_IFC = Pattern.compile("ve \\S+");

    private final Cli cli;

    public RoutedVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder routedVlanBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Matcher ifMatcher = ROUTER_IFC.matcher(name);
        if (ifMatcher.matches()) {
            parseVlanConfig(blockingRead(DefaultSwitchedVlanConfigReader.SWITCHED_VLAN_CONFIG, cli, instanceIdentifier,
                    readContext), routedVlanBuilder, name, readContext);
        }
    }

    private static void parseVlanConfig(String output, ConfigBuilder routedVlanBuilder,
                                String ifcName, ReadContext readContext) {
        List<Vlan> vlans = (List<Vlan>) readContext.getModificationCache()
                .get(DefaultSwitchedVlanConfigReader.VLAN_KEY);
        if (vlans == null) {
            readContext.getModificationCache().put(DefaultSwitchedVlanConfigReader.VLAN_KEY,
                    vlans = ParsingUtils.NEWLINE.splitAsStream(DefaultSwitchedVlanConfigReader.NEWLINE.matcher(output)
                            .replaceAll(" "))
                    .map(Vlan::create)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        vlans.forEach(v -> Vlan.buildConfiguration(ifcName, routedVlanBuilder, v));
    }
}
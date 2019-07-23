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

package io.frinx.cli.unit.brocade.ifc.handler.switchedvlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SwitchedVlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SWITCHED_VLAN_CONFIG = "show running-config vlan | include vlan | *tagged | router*";

    private static final Pattern IFC = Pattern.compile("e(the|thernet)? (?<interface>\\d/\\d+)");
    private static final Pattern NEWLINE = Pattern.compile(" \n ");
    private static final String VLAN_KEY = "VLAN_KEY";

    private final Cli cli;

    public SwitchedVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
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
                    .collect(Collectors.toList()));
        }
        vlans.forEach(sw -> Vlan.buildConfiguration(ifcName, configBuilder, sw));

        configBuilder.setTrunkVlans(Vlan.optimizeTrunkVlans(configBuilder.getTrunkVlans()));
    }
}

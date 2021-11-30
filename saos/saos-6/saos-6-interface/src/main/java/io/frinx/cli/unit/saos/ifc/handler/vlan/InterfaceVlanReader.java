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

package io.frinx.cli.unit.saos.ifc.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_INTERFACE_SHOW = "interface show %s";
    private final Cli cli;

    public InterfaceVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        setVlanIds(blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx),
                configBuilder, ifcName);
        // only remote management interface can contain vlan value
        if (ifcName.equals("remote")) {
            setManagementVlanIds(blockingRead(f(SH_INTERFACE_SHOW, ifcName), cli, id, ctx), configBuilder);
        } else {
            setManagementVlanIds(blockingRead(f(SH_INTERFACE_SHOW, "ip-interface " + ifcName), cli, id, ctx),
                    configBuilder);
        }
    }

    void setManagementVlanIds(final String output, final ConfigBuilder builder) {
        Pattern domainVlan = Pattern.compile("\\| Domain *\\| (?<domain>.+) *\\|");
        final Optional<String> domain = ParsingUtils.parseField(output, 0,
            domainVlan::matcher,
            matcher -> matcher.group("domain"));
        if (domain.isPresent() && !domain.get().contains("n/a")) {
            List<VlanSwitchedConfig.TrunkVlans> trunkVlans = new ArrayList<>();
            trunkVlans.add(new VlanSwitchedConfig.TrunkVlans(
                new VlanId(Integer.valueOf(domain.get().split(" ")[1]))
            ));
            builder.setTrunkVlans(trunkVlans);
        }
    }

    @VisibleForTesting
    public void setVlanIds(final String output, final ConfigBuilder builder, final String name) {
        Pattern portVlans = Pattern.compile("vlan add vlan (?<vlanID>\\S+) port " + name);
        List<String> idsAfterParse = new ArrayList<>();
        List<String> idsBeforeParse = ParsingUtils.parseFields(output, 0,
            portVlans::matcher,
            matcher -> matcher.group("vlanID"),
            value -> value);

        if (!idsBeforeParse.isEmpty()) {
            List<String> vlanIds = new ArrayList<>();
            idsBeforeParse.forEach(id -> vlanIds.addAll(Arrays.asList(id.split(","))));

            vlanIds.forEach(id -> {
                if (id.contains("-")) {
                    String[] indexes = id.split("-");
                    for (int i = Integer.parseInt(indexes[0]); i <= Integer.parseInt(indexes[1]); i++) {
                        idsAfterParse.add(String.valueOf(i));
                    }
                } else {
                    idsAfterParse.add(id);
                }
            });

            List<VlanSwitchedConfig.TrunkVlans> trunkVlans = new ArrayList<>();
            for (String vlanId : idsAfterParse) {
                trunkVlans.add(new VlanSwitchedConfig.TrunkVlans(new VlanId(Integer.valueOf(vlanId))));
            }

            builder.setTrunkVlans(trunkVlans);
        }
    }
}
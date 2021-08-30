/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.ifc.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanRange;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceVlanReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final Pattern SWITCHPORT_ACCESS_VLAN_LINE =
            Pattern.compile("\\s*port default vlan (?<switchportVlan>.+)");
    private static final Pattern LINK_TYPE_LINE = Pattern.compile("\\s*port link-type (?<type>.+)\\s*");
    public static final Pattern SWITCHPORT_TRUNK_ALLOWED_VLAN_LINE =
            Pattern.compile("\\s*port trunk allow-pass vlan (?<allowedVlan>.+)");
    public static final Pattern SWITCHPORT_PVID_VLAN_LINE =
            Pattern.compile("\\s*port trunk pvid vlan (?<pvidVlan>.+)");

    private final Cli cli;

    public InterfaceVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        final String ifcName = id.firstKeyOf(Interface.class).getName();
        parseVlanInterface(blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx),
                configBuilder);
    }

    @VisibleForTesting
    static void parseVlanInterface(final String output, final ConfigBuilder configBuilder) {
        setSwitchportAccessVlan(output, configBuilder);
        setLinkType(output, configBuilder);
        setSwitchportTrunkAllowedVlan(output, configBuilder);
        setSwitchportPvidVlan(output, configBuilder);
    }

    private static void setSwitchportAccessVlan(String output, ConfigBuilder configBuilder) {
        Optional<String> accessVlanValue = ParsingUtils.parseField(output, 0,
            SWITCHPORT_ACCESS_VLAN_LINE::matcher,
            matcher -> matcher.group("switchportVlan"));
        accessVlanValue.ifPresent(s -> configBuilder.setAccessVlan(new VlanId(Integer.parseInt(s))));
    }

    private static void setSwitchportPvidVlan(String output, ConfigBuilder configBuilder) {
        Optional<String> accessVlanValue = ParsingUtils.parseField(output, 0,
            SWITCHPORT_PVID_VLAN_LINE::matcher,
            matcher -> matcher.group("pvidVlan"));
        accessVlanValue.ifPresent(s -> configBuilder.setNativeVlan(new VlanId(Integer.parseInt(s))));
    }

    private static void setSwitchportTrunkAllowedVlan(String output, ConfigBuilder configBuilder) {
        Optional<String> allowedValues = ParsingUtils.parseField(output, 0,
            SWITCHPORT_TRUNK_ALLOWED_VLAN_LINE::matcher,
            matcher -> matcher.group("allowedVlan"));

        allowedValues.ifPresent(s -> configBuilder.setTrunkVlans(getSwitchportTrunkAllowedVlanList(s)));
    }

    private static void setLinkType(String output, ConfigBuilder configBuilder) {
        final Optional<String> modeValue = ParsingUtils.parseField(output, 0,
            LINK_TYPE_LINE::matcher,
            matcher -> matcher.group("type"));

        if (modeValue.isPresent()) {
            VlanModeType vlanModeType;
            switch (modeValue.get()) {
                case "trunk":
                    vlanModeType = VlanModeType.TRUNK;
                    break;
                case "access":
                    vlanModeType = VlanModeType.ACCESS;
                    break;
                default:
                    throw new IllegalArgumentException("Cannot parse link-type value: " + modeValue.get());
            }
            configBuilder.setInterfaceMode(vlanModeType);
        }
    }

    @VisibleForTesting
    static List<TrunkVlans> getSwitchportTrunkAllowedVlanList(String allowedValues) {
        if (allowedValues.contains("to")) {
            List<VlanSwitchedConfig.TrunkVlans> trunkVlans = new ArrayList<>();
            String[] vlans = allowedValues.split(" to ");
            trunkVlans.add(new VlanSwitchedConfig.TrunkVlans(new VlanRange(vlans[0] + ".." + vlans[1])));
            return trunkVlans;
        }
        return null;
    }

}

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

package io.frinx.cli.unit.dasan.ifc.handler.ethernet.vlanmember;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.dasan.ifc.handler.InterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceConfigWriter;
import io.frinx.cli.unit.dasan.ifc.handler.PhysicalPortInterfaceReader;
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember.BundleEtherLacpMemberConfigReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig.TrunkVlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.SwitchedVlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanModeType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PhysicalPortVlanMemberConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    @VisibleForTesting
    static final String SHOW_VLAN_ADD = "show running-config bridge | include ^ vlan add ";
    private static final Pattern VLAN_ADD_LINE_PATTERN = Pattern
            .compile("vlan add (?<ids>\\S+)\\s+(?<ports>\\S+)\\s+(?<vlanmode>(un)?tagged)");
    private final Cli cli;

    public PhysicalPortVlanMemberConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull ConfigBuilder builder,
            @Nonnull ReadContext ctx) throws ReadFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PhysicalPortInterfaceReader.PHYSICAL_PORT_NAME_PATTERN.matcher(ifcName);

        if (!matcher.matches()) {
            return;
        }
        String portId = matcher.group("portid");

        if (!PhysicalPortInterfaceConfigWriter.PHYS_IFC_TYPES.contains(InterfaceReader.parseTypeByName(ifcName))) {
            return;
        }

        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, this, id, ctx);

        // If the target physical port is assigned to lacp, you should skip to check the VLAN assignment.
        // It is checked by TrunkPortVlanMemberConfigReader.
        if (BundleEtherLacpMemberConfigReader.getAssignedLacpId(portId, ports, this, cli, id, ctx).isPresent()) {
            return;
        }
        parseEthernetConfig(blockingRead(SHOW_VLAN_ADD, cli, id, ctx), builder, ports, portId);
    }

    @VisibleForTesting
    static void parseEthernetConfig(String output, ConfigBuilder builder, List<String> ports, String portId) {
        Map<String, List<String>> vlansmap = ParsingUtils.NEWLINE
                .splitAsStream(output)
                .map(String::trim)
                .map(VLAN_ADD_LINE_PATTERN::matcher)
                .filter(Matcher::matches)
                .filter(m -> DasanCliUtil.containsPort(ports, m.group("ports"), portId))
                .collect(Collectors.groupingBy(
                    m -> m.group("vlanmode"),
                    Collectors.mapping(m -> m.group("ids"),
                    Collectors.toList())));

        List<String> nativeIds = vlansmap.get("untagged");
        List<String> trunkIds = vlansmap.get("tagged");
        if (nativeIds != null) {
            List<TrunkVlans> natives = vlansmap.get("untagged").stream()
                    .map(s -> StringUtils.removeAll(s, "br"))
                    .filter(s -> !"default".equals(s))
                    .flatMap(s -> DasanCliUtil.parseIdRanges(s).stream()).map(Integer::valueOf).map(VlanId::new)
                    .map(TrunkVlans::new).collect(Collectors.toList());

            if (!natives.isEmpty()) {
                builder.setNativeVlan(natives.get(0).getVlanId());
            }
        }
        if (trunkIds != null) {
            List<TrunkVlans> trunks = vlansmap.get("tagged").stream()
                    .map(s -> StringUtils.removeAll(s, "br"))
                    .filter(s -> !"default".equals(s))
                    .flatMap(s -> DasanCliUtil.parseIdRanges(s).stream())
                    .map(Integer::valueOf).map(VlanId::new)
                    .map(TrunkVlans::new).collect(Collectors.toList());
            if (trunks.isEmpty()) {
                return;
            }
            builder.setTrunkVlans(trunks);
        }
        if (builder.getNativeVlan() != null || builder.getTrunkVlans() != null) {
            builder.setInterfaceMode(VlanModeType.TRUNK);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((SwitchedVlanBuilder) parentBuilder).setConfig(readValue);
    }
}

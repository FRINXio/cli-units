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
import io.frinx.cli.unit.dasan.ifc.handler.ethernet.lacpmember.BundleEtherLacpMemberConfigReader;
import io.frinx.cli.unit.dasan.utils.DasanCliUtil;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
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

public class TrunkPortVlanMemberConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    @VisibleForTesting
    static final String SHOW_VLAN_ADD = "show running-config bridge | include ^ vlan add ";
    private static final Pattern VLAN_ADD_LINE_PATTERN = Pattern
            .compile("vlan add (?<ids>\\S+)\\s+(?<ports>\\S+)\\s+(?<vlanmode>(un)?tagged)");
    public static final Pattern PORT_NAME_PATTERN = Pattern.compile("Trunk(?<portid>.*)$");

    private final Cli cli;

    public TrunkPortVlanMemberConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull ConfigBuilder builder,
            @NotNull ReadContext ctx) throws ReadFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        Matcher matcher = PORT_NAME_PATTERN.matcher(ifcName);

        if (!matcher.matches()) {
            return;
        }
        String portId = "t/" + matcher.group("portid");

        // If the target trunk port is configured as lacp,
        // you need to check the assignment to the VLAN using any lacp member's physical port.
        String lacpId = convertTrunkIdToLacpId(matcher.group("portid"));
        List<String> lacpMembers = BundleEtherLacpMemberConfigReader.getPortMembers(lacpId, this, cli, id, ctx);
        if (lacpMembers != null && !lacpMembers.isEmpty()) {
            portId = lacpMembers.get(0);
        }

        List<String> ports = DasanCliUtil.getPhysicalPorts(cli, this, id, ctx);
        parseTrunkConfig(blockingRead(SHOW_VLAN_ADD, cli, id, ctx), builder, ports, portId);
    }

    @VisibleForTesting
    static void parseTrunkConfig(String output, ConfigBuilder builder, List<String> ports, String portId) {
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
            List<TrunkVlans> natives = nativeIds.stream()
                    .map(s -> StringUtils.removeAll(s, "br"))
                    .filter(s -> !"default".equals(s))
                    .flatMap(s -> DasanCliUtil.parseIdRanges(s).stream())
                    .map(Integer::valueOf)
                    .map(VlanId::new)
                    .map(TrunkVlans::new)
                    .collect(Collectors.toList());

            if (!natives.isEmpty()) {
                builder.setNativeVlan(natives.get(0).getVlanId());
            }
        }
        if (trunkIds != null) {
            List<TrunkVlans> trunks = trunkIds.stream()
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
    public void merge(@NotNull Builder<? extends DataObject> parentBuilder, @NotNull Config readValue) {
        ((SwitchedVlanBuilder) parentBuilder).setConfig(readValue);
    }

    private static String convertTrunkIdToLacpId(String trunkId) {
        return String.valueOf(Integer.parseInt(trunkId) - 1);
    }
}
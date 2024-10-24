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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupAfiSafiApplyPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_NEI = "show running-config router bgp %s %s %s neighbor-group %s address-family %s";
    private static final Pattern ROUTE_POLICY_IN_LINE = Pattern.compile("route-policy (?<policyName>.+) in");
    private static final Pattern ROUTE_POLICY_OUT_LINE = Pattern.compile("route-policy (?<policyName>.+) out");

    private Cli cli;

    public PeerGroupAfiSafiApplyPolicyConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> iid,
                                             @NotNull ConfigBuilder configBuilder,
                                             @NotNull ReadContext context) throws ReadFailedException {
        Long asNumber = PeerGroupListReader.readAsNumberFromContext(iid, context);
        Preconditions.checkNotNull(asNumber);
        String groupName = iid.firstKeyOf(PeerGroup.class).getPeerGroupName();
        String afiName = GlobalAfiSafiReader.transformAfiToString(iid.firstKeyOf(AfiSafi.class)
            .getAfiSafiName());
        final String protName = iid.firstKeyOf(Protocol.class).getName();
        final String instance = BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(protName)
            ? "" : String.format("instance %s", protName);
        String nwInsName = GlobalConfigWriter.resolveVrfWithName(iid);
        String output = blockingRead(
            f(SH_NEI, asNumber,instance, nwInsName, groupName, afiName),
            cli,
            iid,
            context);
        read(output, configBuilder);
    }

    @VisibleForTesting
    public void read(String output, @NotNull ConfigBuilder configBuilder) {
        List<String> importPolicy = ParsingUtils.parseFields(
            output,
            0,
            ROUTE_POLICY_IN_LINE::matcher,
            matcher -> matcher.group("policyName"),
            v -> v);

        if (importPolicy.size() > 0) {
            configBuilder.setImportPolicy(importPolicy);
        }
        List<String> exportPolicy = ParsingUtils.parseFields(
            output,
            0,
            ROUTE_POLICY_OUT_LINE::matcher,
            matcher -> matcher.group("policyName"),
            v -> v);
        if (exportPolicy.size() > 0) {
            configBuilder.setExportPolicy(exportPolicy);
        }
    }
}
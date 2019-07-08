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

package io.frinx.cli.unit.iosxr.bgp.handler.neighbor;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiApplyPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String NEXTHOPSELF_POLICY_NAME = "nexthopself";
    private static final String SH_NEI = "show running-config router bgp %s %s %s neighbor %s address-family %s";
    private static final Pattern ROUTE_POLICY_IN_LINE = Pattern.compile("route-policy (?<policyName>.+) in");
    private static final Pattern ROUTE_POLICY_OUT_LINE = Pattern.compile("route-policy (?<policyName>.+) out");
    private static final Pattern NEXT_HOP_SELF_LINE = Pattern.compile("next-hop-self");

    private Cli cli;

    public NeighborAfiSafiApplyPolicyConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orNull();

        if (globalConfig == null) {
            return;
        }

        IpAddress neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress();
        String address = new String(neighborIp.getValue());
        final String instName = GlobalConfigWriter.getProtoInstanceName(instanceIdentifier);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        String afiName = GlobalAfiSafiReader.transformAfiToString(instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName());

        String output = blockingRead(String.format(SH_NEI, globalConfig.getAs()
                .getValue()
                .intValue(), instName, nwInsName, address, afiName), cli, instanceIdentifier, readContext);

        List<String> importPolicy = ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(ROUTE_POLICY_IN_LINE::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group("policyName"))
                .collect(Collectors.toList());

        if (importPolicy.size() > 0) {
            configBuilder.setImportPolicy(importPolicy);
        }

        List<String> exportPolicy;
        exportPolicy = ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(ROUTE_POLICY_OUT_LINE::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group("policyName"))
                .collect(Collectors.toList());

        if (ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(NEXT_HOP_SELF_LINE::matcher)
                .anyMatch(Matcher::find)) {
            exportPolicy.add(NEXTHOPSELF_POLICY_NAME);
        }

        if (exportPolicy.size() > 0) {
            configBuilder.setExportPolicy(exportPolicy);
        }
    }
}

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

package io.frinx.cli.unit.iosxr.bgp.handler.local.aggregates;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.NiProtAggAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.NiProtAggAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.Aggregate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.aggregate.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpLocalAggregateReader implements CliConfigListReader<Aggregate, AggregateKey, AggregateBuilder> {

    private static final String SH_BGP = "show running-config router bgp %s %s %s";
    private static final Pattern NETWORK_LINE = Pattern.compile("network (?<prefix>\\S+)(?<policy> route-policy "
            + "(?<policyName>\\S+))*");

    private Cli cli;

    public BgpLocalAggregateReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    @NotNull
    public List<AggregateKey> getAllIds(@NotNull InstanceIdentifier<Aggregate> instanceIdentifier, @NotNull
            ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(instanceIdentifier.firstIdentifierOf(Protocol.class)
                .child(Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orElse(null);

        if (globalConfig
                == null) {
            return Collections.emptyList();
        }

        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        final String instName = GlobalConfigWriter.getProtoInstanceName(instanceIdentifier);
        String output = blockingRead(String.format(SH_BGP, globalConfig.getAs()
                .getValue()
                .intValue(), instName, nwInsName), cli, instanceIdentifier, readContext);

        return ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(NETWORK_LINE::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group("prefix"))
                .map(prefix -> new AggregateKey(new IpPrefix(prefix.toCharArray())))
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Aggregate> instanceIdentifier, @NotNull
            AggregateBuilder builder, @NotNull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(instanceIdentifier.firstIdentifierOf(Protocol.class)
                .child(Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orElse(null);
        final String instName = GlobalConfigWriter.getProtoInstanceName(instanceIdentifier);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        AggregateKey key = instanceIdentifier.firstKeyOf(Aggregate.class);
        String output = blockingRead(String.format(SH_BGP, globalConfig.getAs()
                .getValue()
                .intValue(), instName, nwInsName), cli, instanceIdentifier, readContext);

        Optional<String> policies = ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(NETWORK_LINE::matcher)
                .filter(Matcher::find)
                .filter(matcher -> matcher.group("prefix")
                        .equals(new String(key.getPrefix()
                                .getValue()))
                        && matcher.group("policy")
                        != null)
                .map(matcher -> matcher.group("policyName"))
                .findFirst();

        builder.setPrefix(key.getPrefix());
        ConfigBuilder configBuilder = new ConfigBuilder().setPrefix(key.getPrefix());
        policies.ifPresent(s -> configBuilder.addAugmentation(NiProtAggAug.class, new NiProtAggAugBuilder()
                .setApplyPolicy(Lists.newArrayList(s))
                .build()));
        builder.setConfig(configBuilder.build());
    }
}
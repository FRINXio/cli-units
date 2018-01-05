/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.local.aggregates;

import static io.frinx.cli.ios.bgp.handler.BgpProtocolReader.DEFAULT_BGP_INSTANCE;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.LocalAggregatesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.Aggregate;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.AggregateKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.NeighborReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;

public class BgpLocalAggregateReader implements CliListReader<Aggregate, AggregateKey, AggregateBuilder> {

    private static final String GROUP_IP = "ip";
    private static final String GROUP_MASK = "mask";
    static final String SH_SUMM = "sh run | include ^router bgp|^ *address-family|^ *network";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("network (?<ip>\\S*) mask (?<mask>\\S*).*");
    private final Cli cli;

    public BgpLocalAggregateReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static List<AggregateKey> getVrfAggregateKeys(String output, String vrfName) {
        Optional<String> optionalVrfOutput =
            Arrays.stream(NeighborReader.getSplitedOutput(output)).filter(value -> value.contains(vrfName)).findFirst();

        if(optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll("network", "\nnetwork"), 0,
                NEIGHBOR_LINE::matcher,
                BgpLocalAggregateReader::resolveGroups,
                value -> new AggregateKey(new IpPrefix(new Ipv4Prefix(parseNetworkPrefix(value)))));
        }
        return new ArrayList<>();
    }

    @VisibleForTesting
    static List<AggregateKey> getDefaultAggregateKeys(String output) {
        String[] vrfSplit = NeighborReader.getSplitedOutput(output);
        if (vrfSplit.length > 1 && vrfSplit[0] != null) {
            String defaultInstance = vrfSplit[0];
            return ParsingUtils.parseFields(defaultInstance.replaceAll("network", "\nnetwork"), 0,
                NEIGHBOR_LINE::matcher,
                BgpLocalAggregateReader::resolveGroups,
                value -> new AggregateKey(new IpPrefix(new Ipv4Prefix(parseNetworkPrefix(value)))));
        }
        return new ArrayList<>();
    }

    private static String parseNetworkPrefix(HashMap<String, String> params) {
        SubnetUtils utils = new SubnetUtils(params.get(GROUP_IP), params.get(GROUP_MASK));
        return utils.getInfo().getCidrSignature();
    }

    private static HashMap<String, String> resolveGroups(Matcher m) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(GROUP_IP, m.group(GROUP_IP));
        hashMap.put(GROUP_MASK, m.group(GROUP_MASK));

        return hashMap;
    }

    @Nonnull @Override public List<AggregateKey> getAllIds(@Nonnull InstanceIdentifier<Aggregate> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {
        String niName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);

        if (protocolKey.getIdentifier().equals(BGP.class)) {
            if (DEFAULT_BGP_INSTANCE.equals(niName)) {
                return getDefaultAggregateKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext));
            } else {
                return getVrfAggregateKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext), niName);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Aggregate> list) {
        ((LocalAggregatesBuilder) builder).setAggregate(list);

    }

    @Override public void readCurrentAttributes(@Nonnull InstanceIdentifier<Aggregate> instanceIdentifier,
        @Nonnull AggregateBuilder aggregateBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        aggregateBuilder.setKey(instanceIdentifier.firstKeyOf(Aggregate.class));
    }
}

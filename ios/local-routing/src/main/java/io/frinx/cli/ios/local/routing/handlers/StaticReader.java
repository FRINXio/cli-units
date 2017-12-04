/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrListReader;
import io.frinx.cli.unit.utils.ParsingUtils;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticReader implements LrListReader.LrConfigListReader<Static, StaticKey, StaticBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "sh run | include ip route";
    private static final Pattern IP_PREFIX_LINE_DEFAULT =
            Pattern.compile("ip route (?<ip>(?!vrf)[\\S]+) (?<mask>[\\S]+).*");
    private static final Pattern IP_PREFIX_LINE_VRF =
            Pattern.compile("ip route vrf (?<vrf>[\\S]+) (?<ip>[\\S]+) (?<mask>[\\S]+).*");
    private static final String GROUP_MASK = "mask";
    private static final String GROUP_IP = "ip";
    private static final String GROUP_VRF = "vrf";

    private Cli cli;

    public StaticReader(final Cli cli) {
        this.cli = cli;
    }

    private static StaticKey resolveStaticKey(HashMap<String, String> value) {
        SubnetUtils subnetUtils = new SubnetUtils(value.get(GROUP_IP), value.get(GROUP_MASK));
        return new StaticKey(new IpPrefix(new Ipv4Prefix(subnetUtils.getInfo().getCidrSignature())));
    }

    private static HashMap<String, String> resolveGroupsVrf(Matcher m) {
        HashMap<String, String> groups = resolveGroups(m);
        groups.put(GROUP_VRF, m.group(GROUP_VRF));
        return groups;
    }

    private static HashMap<String, String> resolveGroups(Matcher m) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(GROUP_IP, m.group(GROUP_IP));
        hashMap.put(GROUP_MASK, m.group(GROUP_MASK));

        return hashMap;
    }

    @Nonnull
    @Override
    public List<StaticKey> getAllIdsForType(@Nonnull InstanceIdentifier<Static> instanceIdentifier,
                                            @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(Protocol.class).getName();

        return parseStaticPrefixes(blockingRead(SH_IP_STATIC_ROUTE, cli, instanceIdentifier, readContext), vrfName);
    }

    @VisibleForTesting
    static List<StaticKey> parseStaticPrefixes(String output, String vrfName) {
        Pattern matchPattern = vrfName.equals(DEFAULT_NETWORK_NAME) ? IP_PREFIX_LINE_DEFAULT : IP_PREFIX_LINE_VRF;

        if (vrfName.equals(DEFAULT_NETWORK_NAME)) {
            return ParsingUtils.parseFields(output, 0, matchPattern::matcher, StaticReader::resolveGroups,
                StaticReader::resolveStaticKey);
        } else {
            return ParsingUtils.parseFields(output, 0,
                matchPattern::matcher,
                StaticReader::resolveGroupsVrf,
                StaticReader::resolveStaticKey,
                groupsHashMap -> groupsHashMap.get(GROUP_VRF).equals(vrfName));
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Static> list) {
        ((StaticRoutesBuilder) builder).setStatic(list);
    }

    @Nonnull
    @Override
    public StaticBuilder getBuilder(@Nonnull InstanceIdentifier<Static> instanceIdentifier) {
        return new StaticBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Static> instanceIdentifier,
                                             @Nonnull StaticBuilder staticBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        staticBuilder.setPrefix(instanceIdentifier.firstKeyOf(Static.class).getPrefix());
    }

}

/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
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
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticReader implements LrListReader.LrConfigListReader<Static, StaticKey, StaticBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "sh run | include ip route|ipv6 route";

    static final Pattern ROUTE_LINE_IP =
            Pattern.compile("ip route (vrf \\S+)?\\s*(?<net>[\\d.]+)\\s*(?<mask>[\\d.]+)\\s*(?<ifc>[A-Z][\\w.]+)?\\s*(?<ip>[\\d]+.[\\d.]+)?\\s*(?<metric>[\\d]+)?.*");
    static final Pattern ROUTE_LINE_IP6 =
            Pattern.compile("ipv6 route (vrf \\S+)?\\s*(?<net>[\\d:/]+)\\s*(?<ifc>[A-Z][\\w.]+)?\\s*(?<ip>\\d*:[\\d:]+)?\\s*(?<metric>[\\d]+)?.*");

    private static final String GROUP_MASK = "mask";
    private static final String GROUP_IP = "net";
    private static final String GROUP_VRF = "vrf";

    private Cli cli;

    public StaticReader(final Cli cli) {
        this.cli = cli;
    }

    private static StaticKey resolveStaticKey(HashMap<String, String> value) {
        String prefix;

        if (value.containsKey(GROUP_MASK)) {
            SubnetUtils subnetUtils = new SubnetUtils(value.get(GROUP_IP), value.get(GROUP_MASK));
            prefix = subnetUtils.getInfo().getCidrSignature();
        } else {
            prefix = value.get(GROUP_IP);
        }

        return new StaticKey(new IpPrefix(prefix.toCharArray()));
    }

    private static HashMap<String, String> resolveGroups(Matcher m) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(GROUP_IP, m.group(GROUP_IP));

        if (m.pattern() == ROUTE_LINE_IP) {
            hashMap.put(GROUP_MASK, m.group(GROUP_MASK));
        }

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
        output = filterVrf(output, vrfName);

        return ParsingUtils.parseFields(output, 0,
                StaticReader::getMatcher,
                StaticReader::resolveGroups,
                StaticReader::resolveStaticKey);
    }

    static String filterVrf(String output, String vrfKey) {
        Predicate<? super String> filter = vrfKey.equals(NetworInstance.DEFAULT_NETWORK_NAME) ?
                s -> !s.contains("route vrf ") :
                s -> s.contains("route vrf " + vrfKey);

        output = ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(filter)
                .reduce((s, s2) -> String.format("%s\n%s", s, s2))
                .orElse("");
        return output;
    }

    static Matcher getMatcher(String s) {
        Matcher matcher = ROUTE_LINE_IP.matcher(s);
        return matcher.matches() ? matcher : ROUTE_LINE_IP6.matcher(s);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Static> list) {
        ((StaticRoutesBuilder) builder).setStatic(list);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Static> instanceIdentifier,
                                             @Nonnull StaticBuilder staticBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        staticBuilder.setPrefix(instanceIdentifier.firstKeyOf(Static.class).getPrefix());
    }

}

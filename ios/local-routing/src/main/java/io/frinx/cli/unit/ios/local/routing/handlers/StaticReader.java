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

package io.frinx.cli.unit.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.util.SubnetUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticReader implements CliConfigListReader<Static, StaticKey, StaticBuilder> {

    private static final String SH_IP_STATIC_ROUTE = "show running-config | include ip route|ipv6 route";

    static final Pattern ROUTE_LINE_IP =
            Pattern.compile("ip route (vrf \\S+)?\\s*(?<net>[\\d.]+)\\s*(?<mask>[\\d.]+)\\s*(?<ifc>[A-Z][\\w.]+)?\\s*"
                    + "(?<ip>[\\d]+.[\\d.]+)?\\s*(?<metric>[\\d]+)?.*");
    static final Pattern ROUTE_LINE_IP6 =
            Pattern.compile("ipv6 route (vrf \\S+)?\\s*(?<net>[\\d:/A-F]+)\\s*(?<ifc>[A-Z][\\w.]+)?"
                    + "\\s*(?<ip>\\d*:[\\d:A-F]+)?\\s*(?<metric>[\\d]+)?.*");

    private static final String GROUP_MASK = "mask";
    private static final String GROUP_IP = "net";

    private Cli cli;

    public StaticReader(final Cli cli) {
        this.cli = cli;
    }

    private static StaticKey resolveStaticKey(HashMap<String, String> value) {
        String prefix;

        if (value.containsKey(GROUP_MASK)) {
            SubnetUtils subnetUtils = new SubnetUtils(value.get(GROUP_IP), value.get(GROUP_MASK));
            prefix = subnetUtils.getInfo()
                    .getCidrSignature();
        } else {
            prefix = value.get(GROUP_IP);
        }

        return new StaticKey(new IpPrefix(prefix.toCharArray()));
    }

    private static HashMap<String, String> resolveGroups(Matcher matcher) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(GROUP_IP, matcher.group(GROUP_IP));

        if (matcher.pattern() == ROUTE_LINE_IP) {
            hashMap.put(GROUP_MASK, matcher.group(GROUP_MASK));
        }

        return hashMap;
    }

    @NotNull
    @Override
    public List<StaticKey> getAllIds(@NotNull InstanceIdentifier<Static> instanceIdentifier,
                                            @NotNull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

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
        Predicate<? super String> filter = vrfKey.equals(NetworInstance.DEFAULT_NETWORK_NAME)
                ?
                    s -> !s.contains("route vrf ") :
                    s -> s.contains("route vrf " + vrfKey);

        output = ParsingUtils.NEWLINE.splitAsStream(output)
                .filter(filter)
            .reduce((s1, s2) -> String.format("%s%n%s", s1, s2))
                .orElse("");
        return output;
    }

    static Matcher getMatcher(String string) {
        Matcher matcher = ROUTE_LINE_IP.matcher(string);
        return matcher.matches() ? matcher : ROUTE_LINE_IP6.matcher(string);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Static> instanceIdentifier,
                                             @NotNull StaticBuilder staticBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        staticBuilder.setPrefix(instanceIdentifier.firstKeyOf(Static.class)
                .getPrefix());
    }
}
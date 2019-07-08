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

package io.frinx.cli.unit.ospf.handler.table;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.INSTALLPROTOCOLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfTableConnectionReader implements
        CliConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder>,
        CompositeListReader.Child<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    static final String SH_OSPF_REDIS =
            "show running-config | include ^router ospf|^ redistribute";

    static final Pattern REDIS_LINE =
            Pattern.compile(".*redistribute (?<protocol>\\S+) (?<protocolId>\\S+).*");

    static final Pattern POLICY_LINE =
            Pattern.compile(".*route-map (?<policy>\\S+).*");

    private Cli cli;

    public OspfTableConnectionReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<TableConnectionKey> getAllIds(@Nonnull InstanceIdentifier<TableConnection> id,
                                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String output = blockingRead(SH_OSPF_REDIS, cli, id, readContext);
        return parseRedistributes(vrfKey, output);
    }

    static List<TableConnectionKey> parseRedistributes(NetworkInstanceKey vrfKey, String output) {
        output = realignOutput(output);
        Predicate<String> vrfMatch = getVrfMatch(vrfKey);

        return getRedistributes(output, vrfMatch)
                .map(Map.Entry::getKey)
                .distinct()
                .collect(Collectors.toList());
    }

    private static Predicate<String> getVrfMatch(NetworkInstanceKey vrfKey) {
        return vrfKey.equals(NetworInstance.DEFAULT_NETWORK)
                ?
                    s -> !s.contains("vrf") :
                    s -> s.contains("vrf " + vrfKey.getName());
    }

    private static Stream<Map.Entry<TableConnectionKey, Config>> getRedistributes(String output, Predicate<String>
            vrf) {
        return ParsingUtils.NEWLINE.splitAsStream(output)
                // Skip header line(s)
                .map(String::trim)
                .filter(vrf)
                .flatMap(line -> ParsingUtils.NEWLINE.splitAsStream(line.replaceAll("redistribute", "\nredistribute")))
                .map(String::trim)
                .map(REDIS_LINE::matcher)
                .filter(Matcher::matches)
                .map(OspfTableConnectionReader::toKey)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static Optional<Map.Entry<TableConnectionKey, Config>> toKey(Matcher matcher) {
        Optional<Class<? extends INSTALLPROTOCOLTYPE>> protocol = transformProtocol(matcher.group("protocol"));

        Matcher policyMatcher = POLICY_LINE.matcher(matcher.group(0));
        List<String> policies = policyMatcher.matches()
                ?
                transformPolicies(policyMatcher.group("policy")) :
                Collections.emptyList();

        if (protocol.isPresent()) {
            return Optional.of(new AbstractMap.SimpleEntry<>(
                    new TableConnectionKey(IPV4.class, OSPF.class, protocol.get()),
                    new ConfigBuilder()
                            .setAddressFamily(IPV4.class)
                            .setSrcProtocol(protocol.get())
                            .setDstProtocol(OSPF.class)
                            .setImportPolicy(policies)
                            .build()));
        }

        return Optional.empty();
    }

    private static List<String> transformPolicies(String policy) {
        return policy == null ? Collections.emptyList() : Lists.newArrayList(policy);
    }

    private static Optional<Class<? extends INSTALLPROTOCOLTYPE>> transformProtocol(String protocol) {
        switch (protocol) {
            case "ospf":
                return Optional.of(OSPF.class);
            case "bgp":
                return Optional.of(BGP.class);
            default: break;
        }

        return Optional.empty();
    }

    private static String realignOutput(String output) {
        output = output.replaceAll("[\\n\\r]", "");
        output = output.replace("router ospf ", "\nrouter ospf ");
        output = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(s -> s.startsWith("router ospf"))
                .reduce((s1, s2) -> s1 + "\n" + s2)
                .orElse("");
        return output;
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<TableConnection> id,
                                             @Nonnull TableConnectionBuilder tableConnectionBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        String output = blockingRead(SH_OSPF_REDIS, cli, id, readContext);
        output = realignOutput(output);

        Predicate<String> vrf = getVrfMatch(vrfKey);

        getRedistributes(output, vrf)
                .filter(e -> e.getKey().equals(id.firstKeyOf(TableConnection.class)))
                .map(Map.Entry::getValue)
                .findFirst()
                .ifPresent(c -> {
                    TableConnectionKey tableConnectionKey = id.firstKeyOf(TableConnection.class);
                    tableConnectionBuilder.setDstProtocol(tableConnectionKey.getDstProtocol());
                    tableConnectionBuilder.setSrcProtocol(tableConnectionKey.getSrcProtocol());
                    tableConnectionBuilder.setAddressFamily(tableConnectionKey.getAddressFamily());
                    tableConnectionBuilder.setConfig(c);
                });
    }
}

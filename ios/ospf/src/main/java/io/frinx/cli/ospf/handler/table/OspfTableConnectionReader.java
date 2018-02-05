/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf.handler.table;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.openconfig.network.instance.NetworInstance;
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
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfTableConnectionReader implements
        L3VrfListReader.L3VrfConfigListReader<TableConnection, TableConnectionKey, TableConnectionBuilder>,
        CompositeListReader.Child<TableConnection, TableConnectionKey, TableConnectionBuilder> {

    static final String SH_OSPF_REDIS =
            "sh run | include ^router ospf|^ redistribute";

    static final Pattern REDIS_LINE =
            Pattern.compile(".*redistribute (?<protocol>\\S+) (?<protocolId>\\S+).*");

    static final Pattern POLICY_LINE =
            Pattern.compile(".*route-map (?<policy>\\S+).*");

    private Cli cli;

    public OspfTableConnectionReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<TableConnectionKey> getAllIdsForType(@Nonnull InstanceIdentifier<TableConnection> id,
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
        return vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ?
                    s -> !s.contains("vrf") :
                    s -> s.contains("vrf " + vrfKey.getName());
    }

    private static Stream<Map.Entry<TableConnectionKey, Config>> getRedistributes(String output, Predicate<String> vrf) {
        return NEWLINE.splitAsStream(output)
                // Skip header line(s)
                .map(String::trim)
                .filter(vrf)
                .flatMap(line -> NEWLINE.splitAsStream(line.replaceAll("redistribute", "\nredistribute")))
                .map(String::trim)
                .map(REDIS_LINE::matcher)
                .filter(Matcher::matches)
                .map(OspfTableConnectionReader::toKey)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static Optional<Map.Entry<TableConnectionKey, Config>> toKey(Matcher m) {
        Optional<Class<? extends INSTALLPROTOCOLTYPE>> protocol = transformProtocol(m.group("protocol"));

        Matcher policyMatcher = POLICY_LINE.matcher(m.group(0));
        List<String> policies = policyMatcher.matches() ?
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
        }

        return Optional.empty();
    }

    private static String realignOutput(String output) {
        output = output.replaceAll("\\n|\\r", "");
        output = output.replace("router ospf ", "\nrouter ospf ");
        output = NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(s -> s.startsWith("router ospf"))
                .reduce((s, s2) -> s + "\n" + s2)
                .orElse("");
        return output;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder,
                      @Nonnull List<TableConnection> list) {
        // NOOP
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<TableConnection> id,
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

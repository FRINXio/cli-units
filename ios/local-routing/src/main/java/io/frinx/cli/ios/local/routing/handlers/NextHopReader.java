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
import io.frinx.cli.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.NextHopsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import static io.frinx.cli.ios.local.routing.handlers.StaticReader.DEFAULT_VRF;

public class NextHopReader implements CliListReader<NextHop, NextHopKey, NextHopBuilder> {

    private static final String SH_IP_ROUTE = "sh ip static route %s";
    private static final String SH_IP_STATIC_ROUTE_VRF = "sh ip static route vrf %s %s";

    private static final Pattern NEXT_HOP_INTERFACE_LINE =
            Pattern.compile(".+] via (?<interface>\\w+) \\[.+");
    private static final Pattern NEXT_HOP_IP_LINE =
            Pattern.compile(".+] via (?<ipAddress>[\\d*\\.]+) \\[.+");
    private static final Pattern NEXT_HOP_INTERFACE_IP_LINE =
            Pattern.compile(".+] via (?<interface>[\\w[/]]+) (?<ipAddress>[\\d*\\.]+) \\[.+");



    private Cli cli;

    public NextHopReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<NextHopKey> getAllIds(@Nonnull InstanceIdentifier<NextHop> instanceIdentifier,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(StaticLocalRoutingProtocolReader.TYPE)) {
            return Collections.emptyList();
        }

        StaticKey staticRouteKey = instanceIdentifier.firstKeyOf(Static.class);
        String ipPrefix = staticRouteKey.getPrefix().getIpv4Prefix().getValue();

        String showCommand = protocolKey.getName().equals(DEFAULT_VRF)
                ? String.format(SH_IP_ROUTE, ipPrefix)
                : String.format(SH_IP_STATIC_ROUTE_VRF, protocolKey.getName(), ipPrefix);

        return parseNextHopPrefixes(blockingRead(String.format(showCommand, ipPrefix),
                cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<NextHopKey> parseNextHopPrefixes(String output) {
        List<NextHopKey> nextHopKeyes = new ArrayList<>();

        nextHopKeyes.addAll(
                ParsingUtils.parseFields(output, 0,
                        NEXT_HOP_IP_LINE::matcher,
                        matcher -> matcher.group("ipAddress"),
                        NextHopKey::new));

        nextHopKeyes.addAll(
                ParsingUtils.parseFields(output, 0,
                        NEXT_HOP_INTERFACE_LINE::matcher,
                        matcher -> matcher.group("interface"),
                        NextHopKey::new));

        nextHopKeyes.addAll(
                ParsingUtils.parseFields(output, 0,
                        NEXT_HOP_INTERFACE_IP_LINE::matcher,
                        matcher ->
                                String.format("%s %s", matcher.group("ipAddress"),
                                        matcher.group("interface")),
                        NextHopKey::new));

        return nextHopKeyes;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<NextHop> list) {
        ((NextHopsBuilder) builder).setNextHop(list);
    }

    @Nonnull
    @Override
    public NextHopBuilder getBuilder(@Nonnull InstanceIdentifier<NextHop> instanceIdentifier) {
        return new NextHopBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<NextHop> instanceIdentifier,
                                      @Nonnull NextHopBuilder nextHopBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(StaticLocalRoutingProtocolReader.TYPE)) {
            return;
        }

        nextHopBuilder.setIndex(instanceIdentifier.firstKeyOf(NextHop.class).getIndex());
    }
}

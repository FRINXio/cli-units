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
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.unit.utils.InitCliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;

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

public class StaticNextHopReader implements InitCliListReader<NextHop, NextHopKey, NextHopBuilder> {

    private static final String SH_IP_ROUTE= "sh ip route";
    private static final Pattern NEXT_HOP_LINE =
            Pattern.compile("[\\*[\\s]]*(?<nextHopIp>\\d+\\.\\d+\\.\\d+\\.\\d+)");
    private static final Pattern INTERFACE_LINE =
            Pattern.compile(".*directly connected, via (?<interface>\\w+)");

    private Cli cli;

    public StaticNextHopReader(final Cli cli) {
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

        String[] cidrFormat = staticRouteKey.getPrefix().getIpv4Prefix().getValue().split("/");
        String ipAddress = cidrFormat[0];

        return parseNextHop(blockingRead(SH_IP_ROUTE + " " + ipAddress, cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    static List<NextHopKey> parseNextHop(String output) {
        List<NextHopKey> nextHopIpKeys =
                ParsingUtils.parseFields(output, 0,
                        NEXT_HOP_LINE::matcher,
                        m -> m.group("nextHopIp"),
                        NextHopKey::new);

        List<NextHopKey> nextHopInterfaceKeys =
                ParsingUtils.parseFields(output, 0,
                        INTERFACE_LINE::matcher,
                        m -> m.group("interface"),
                        NextHopKey::new);

        nextHopInterfaceKeys.addAll(nextHopIpKeys);

        return nextHopInterfaceKeys;
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

        // TODO parse properly all needed attributes
        nextHopBuilder.setIndex(instanceIdentifier.firstKeyOf(NextHop.class).getIndex());
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<NextHop> id,
                                                  @Nonnull NextHop readValue,
                                                  @Nonnull ReadContext readContext) {
        // Direct translation
        return Initialized.create(id, readValue);
    }
}

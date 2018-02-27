/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import static io.frinx.cli.iosxr.bgp.handler.BgpProtocolReader.DEFAULT_BGP_INSTANCE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class NeighborReader implements BgpListReader.BgpConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private static final String SH_NEI = "do show running-config router bgp %s %s | include ^ neighbor";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("neighbor (?<neighborIp>.+)");

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list).build();
    }

    @Override
    public List<NeighborKey> getAllIdsForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {
        final String protName = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        final String instance = DEFAULT_BGP_INSTANCE.equals(protName) ? "" : String.format("instance %s", protName);

        // TODO Same as in NeighborConfigReader
        final Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(Config.class))
                .orNull();

        if (globalConfig == null || globalConfig.getAs() == null) {
            // TODO Should we fail instead??
            return Collections.emptyList();
        }

        Long as = globalConfig.getAs().getValue();

        return getNeighborKeys(blockingRead(String.format(SH_NEI, as.intValue(), instance),
                cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier, @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }

    @VisibleForTesting
    public static List<NeighborKey> getNeighborKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                NEIGHBOR_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                value -> new NeighborKey(new IpAddress(new Ipv4Address(value.trim()))));
    }
}

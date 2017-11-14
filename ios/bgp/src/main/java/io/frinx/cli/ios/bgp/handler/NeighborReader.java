/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.common.BgpListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class NeighborReader implements BgpListReader.BgpConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    static final String SH_SUMM = "show bgp all summary | begin Neighbor";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("(?<neighborIp>.+) (?<ver>[\\d]+) (?<as>[\\d]+) (?<msgRcvd>[\\d]+) (?<msgSent>[\\d]+) (?<tblVer>[\\d]+) (?<inQ>.+) (?<outQ>.+) (?<time>.+) (?<pfxRcd>.+)");

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list).build();
    }

    @Override
    public List<NeighborKey> getAllIdsForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        return getNeighborKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier, @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }

    @Nonnull
    @Override
    public NeighborBuilder getBuilder(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier) {
        return new NeighborBuilder();
    }

    @VisibleForTesting
    public static List<NeighborKey> getNeighborKeys(String output) {
        return ParsingUtils.parseFields(output.replaceAll("\\h+", " "), 0,
                NEIGHBOR_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                value -> new NeighborKey(new IpAddress(new Ipv4Address(value))));
    }
}

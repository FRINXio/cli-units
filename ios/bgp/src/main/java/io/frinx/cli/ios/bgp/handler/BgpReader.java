/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.BgpNeighborState.SessionState;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.StateBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.PrefixesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.BgpBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.GlobalBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.Ipv4Address;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.yang.rev170403.DottedQuad;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.annotations.VisibleForTesting;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;

public class BgpReader implements CliReader<Bgp, BgpBuilder> {

    private static final String SH_SUMM = "sh bgp summ";
    private static final Pattern CONFIG_LINE = Pattern.compile("BGP router identifier (?<id>.+), local AS number (?<as>.+)");
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("(?<neighborIp>.+) 4 (?<as>.+) (?<msgRcvd>.+) (?<msgSent>.+) (?<tblVer>.+) (?<inQ>.+) (?<outQ>.+) (?<time>.+) (?<pfxRcd>.+)");

    private Cli cli;

    public BgpReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public BgpBuilder getBuilder(InstanceIdentifier<Bgp> arg0) {
        return new BgpBuilder();
    }

    @Override
    public void merge(Builder<? extends DataObject> arg0, Bgp arg1) {
        // NOOP root builder
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Bgp> id, BgpBuilder builder, ReadContext ctx) throws ReadFailedException {
        parseGlobal(blockingRead(SH_SUMM, cli, id), builder);

        List<Neighbor> nList = new ArrayList<>();

        // IPV4 unicast
        parseNeighbors(blockingRead(Ipv4Reader.COMMAND, cli, id), nList, Ipv4Reader.AFI_SAFI);

        // VPNV4 unicast
        parseNeighbors(blockingRead(Vpnv4Reader.COMMAND, cli, id), nList, Vpnv4Reader.AFI_SAFI);

        builder.setNeighbors(new NeighborsBuilder().setNeighbor(nList).build());
    }

    @VisibleForTesting
    public void parseGlobal(String output, BgpBuilder builder) {
        if (output.isEmpty()) {
            LOG.warn("Empty output.");
            return;
        }
        LOG.trace("output : {}", output);
        ConfigBuilder cBuilder = new ConfigBuilder();
        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
                matcher -> matcher.group("id"),
                value -> cBuilder.setRouterId(new DottedQuad(value)));

        ParsingUtils.parseField(output, 0,
                CONFIG_LINE::matcher,
                matcher -> matcher.group("as"),
                value -> cBuilder.setAs(new AsNumber(Long.valueOf(value))));
        builder.setGlobal(new GlobalBuilder().setConfig(cBuilder.build()).build());
    }

    @VisibleForTesting
    public void parseNeighbors(String output, List<Neighbor> nList, Class<? extends AFISAFITYPE> afiSafi) {
        List<IpAddress> ipList = ParsingUtils.parseFields(output, 0,
                NEIGHBOR_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                value -> new IpAddress(new Ipv4Address(value.trim())));

        // State/PfxRcd can be either Idle or number depending on whether the connection is/was established and prefixes were received
        // parse first a string, then decide if it's status or number of prefixes received
        List<String> pfxList = ParsingUtils.parseFields(output, 0,
                NEIGHBOR_LINE::matcher,
                matcher -> matcher.group("pfxRcd"),
                String::trim);

        for (int i = 0; i < ipList.size(); i++) {

            // in case of the neighbor being in various afi/safi, don't overwrite data
            NeighborBuilder nBuilder = findNeighbor(nList, ipList.get(i));
            if (nBuilder.getNeighborAddress() == null) {
                nBuilder.setNeighborAddress(ipList.get(i));
            }
            List<AfiSafi> afiSafis = nBuilder.getAfiSafis() == null ? new ArrayList<>() : nBuilder.getAfiSafis().getAfiSafi();
            AfiSafiBuilder aBuilder = new AfiSafiBuilder();
            aBuilder.setAfiSafiName(afiSafi);
            if (StringUtils.isNumeric(pfxList.get(i))) {

                // we got prefix received
                aBuilder.setState(new StateBuilder().setPrefixes(new PrefixesBuilder().setReceived(Long.valueOf(pfxList.get(i))).build()).build());
            } else {

                // we got state
                nBuilder.setState(new org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.StateBuilder()
                    .setSessionState(SessionState.valueOf(pfxList.get(i).toUpperCase())).build());
            }
            afiSafis.add(aBuilder.build());
            nBuilder.setAfiSafis(new AfiSafisBuilder().setAfiSafi(afiSafis).build());
            nList.add(nBuilder.build());
        }
    }

    private NeighborBuilder findNeighbor(List<Neighbor> nList, IpAddress ip) {
        Optional<Neighbor> optN = nList.stream().filter(n -> n.getNeighborAddress().equals(ip)).findAny();
        if (optN.isPresent()) {
            nList.remove(optN.get());
            return new NeighborBuilder(optN.get());
        }
        return new NeighborBuilder();
    }
}

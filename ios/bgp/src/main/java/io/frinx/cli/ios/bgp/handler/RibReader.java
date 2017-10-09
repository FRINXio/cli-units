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
import java.util.regex.Pattern;

import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.BgpOriginAttrType;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.BgpLocRibCommonKeys.Origin;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRib;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.BgpRibBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafi;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.bgp.rib.top.bgp.rib.afi.safis.afi.safi.Ipv4UnicastBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.LocRibBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.RoutesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.Route;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.annotations.VisibleForTesting;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;

public class RibReader implements CliReader<BgpRib, BgpRibBuilder> {

    private static final String SH_VERSION = "sh ip bgp";
    private static final Pattern ROUTE_LINE = Pattern.compile("(?<statusCodes>[sdh\\*>irSmbfxac\\s]+) (?<prefix>[\\S]+) (?<nextHop>.+) (?:.*) (?<origin>[ie\\?])");

    private Cli cli;

    public RibReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public BgpRibBuilder getBuilder(InstanceIdentifier<BgpRib> id) {
        return new BgpRibBuilder();
        }

    @Override
    public void merge(Builder<? extends DataObject> parentBuilder, BgpRib readValue) {
        // NOOP root builder
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<BgpRib> id, BgpRibBuilder builder, ReadContext ctx)
    throws ReadFailedException {
        ProtocolKey protocolKey = id.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(BgpProtocolReader.TYPE)) {
            return;
        }

        parseRib(blockingRead(SH_VERSION, cli, id), builder);
    }

    @VisibleForTesting
    public void parseRib(String output, BgpRibBuilder builder) {
        LOG.trace("output : {}", output);
        List<String> statusCodes = ParsingUtils.parseFields(output, 0,
                ROUTE_LINE::matcher,
                matcher -> matcher.group("statusCodes"),
                String::trim);

        List<Ipv4Prefix> prefixes = ParsingUtils.parseFields(output, 0,
                ROUTE_LINE::matcher,
                matcher -> matcher.group("prefix"),
                Ipv4Prefix::new);

        List<String> origins = ParsingUtils.parseFields(output, 0,
                ROUTE_LINE::matcher,
                matcher -> matcher.group("origin"),
                String::trim);

        List<Route> routeList = new ArrayList<>();
        for(int i = 0; i < statusCodes.size(); i++) {
            final StateBuilder sBuilder = new StateBuilder();
            sBuilder.setPrefix(prefixes.get(i));

            // * valid
            sBuilder.setValidRoute(statusCodes.get(i).contains(Character.toString('*')));
            routeList.add(new RouteBuilder()
                .setOrigin(origins.get(i))
                .setPathId("0")
                .setPrefix(prefixes.get(i).getValue())
                .setState(sBuilder.build()).build());
        }
        List<AfiSafi> afiSafiList = new ArrayList<>();
        AfiSafiBuilder asBuilder = new AfiSafiBuilder();
        asBuilder.setAfiSafiName(IPV4UNICAST.class);
        asBuilder.setIpv4Unicast(new Ipv4UnicastBuilder().setLocRib(new LocRibBuilder().setRoutes(new RoutesBuilder().setRoute(routeList).build()).build()).build());
        afiSafiList.add(asBuilder.build());
        builder.setAfiSafis(new AfiSafisBuilder().setAfiSafi(afiSafiList).build());
   }
}

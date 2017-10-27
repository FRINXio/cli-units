/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.rib.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.RoutesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.Route;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.RouteKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.rib.bgp.rev161017.ipv4.loc.rib.top.loc.rib.routes.route.StateBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4RoutesReader implements CliOperListReader<Route, RouteKey, RouteBuilder> {

    private static final String SH_IP_BGP = "sh ip bgp";
    private static final String SH_IP_BGP_PREFIX = "sh ip bgp | section %s";
    private static final Pattern ROUTE_LINE = Pattern.compile("(?<statusCodes>[sdh\\*>irSmbfxac\\s]+) (?<prefix>[\\S]+) (?<nextHop>.+) (?:.*) (?<origin>[ie\\?])");
    private static final String PATH_ID = "0";

    private Cli cli;

    public Ipv4RoutesReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    public void parseRoute(String output, RouteBuilder builder, RouteKey key) {
        final StateBuilder sBuilder = new StateBuilder();
        sBuilder.setPrefix(new Ipv4Prefix(key.getPrefix()));

        // * valid
        ParsingUtils.parseField(output, 0,
            ROUTE_LINE::matcher,
            matcher -> matcher.group("statusCodes"))
                .ifPresent(s -> sBuilder.setValidRoute(s.contains(Character.toString('*'))));

        builder.setOrigin(key.getOrigin())
            .setPathId(PATH_ID)
            .setPrefix(key.getPrefix())
            .setState(sBuilder.build()).build();
    }

    @VisibleForTesting
    public List<RouteKey> getRouteKeys(String output) {
        List<Ipv4Prefix> prefixes = ParsingUtils.parseFields(output, 0,
            ROUTE_LINE::matcher,
            matcher -> matcher.group("prefix"),
            Ipv4Prefix::new);

        List<String> origins = ParsingUtils.parseFields(output, 0,
            ROUTE_LINE::matcher,
            matcher -> matcher.group("origin"),
            String::trim);

        List<RouteKey> routeKeys = new ArrayList<>();
        for(int i = 0; i < prefixes.size(); i++) {
            RouteKey r = new RouteKey(origins.get(i), PATH_ID, prefixes.get(i).getValue());
            routeKeys.add(r);
        }
        return routeKeys;
    }

    @Nonnull
    @Override
    public List<RouteKey> getAllIds(@Nonnull InstanceIdentifier<Route> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        return getRouteKeys(blockingRead(SH_IP_BGP, cli, instanceIdentifier, readContext));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Route> list) {
        ((RoutesBuilder) builder).setRoute(list);
    }

    @Nonnull
    @Override
    public RouteBuilder getBuilder(@Nonnull InstanceIdentifier<Route> instanceIdentifier) {
        return new RouteBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Route> instanceIdentifier, @Nonnull RouteBuilder routeBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        RouteKey key = instanceIdentifier.firstKeyOf(Route.class);
        parseRoute(blockingRead(String.format(SH_IP_BGP_PREFIX, key.getPrefix()), cli, instanceIdentifier, readContext), routeBuilder, key);
    }
}

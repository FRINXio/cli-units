/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceStateReader.SH_SINGLE_INTERFACE;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceStateReader.STATUS_LINE;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.cli.unit.utils.ParsingUtils.parseField;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.read.Reader;
import io.frinx.cli.handlers.network.instance.L2p2pReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.EndpointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.Local;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.LocalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.Remote;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.RemoteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.ENDPOINTTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConnectionPointsReader implements L2p2pReader.L2p2pConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    public static final String POINT_1 = "1";
    public static final String ENDPOINT_1_LINE = "End-point " + POINT_1;
    public static final String POINT_2 = "2";
    public static final String ENDPOINT_2_LINE = "End-point " + POINT_2;
    public static final String ENDPOINT_ID = "default";

    private final Cli cli;

    public ConnectionPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConnectionPointsBuilder getBuilder(@Nonnull InstanceIdentifier<ConnectionPoints> id) {
        return new ConnectionPointsBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                             @Nonnull ConnectionPointsBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        boolean isOper = isOper(ctx);
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        List<ConnectionPoint> connectionPoints = getVllPoints(id, ctx, netName, isOper);

        if (connectionPoints.size() == 2) {
            // Try to set xconnect local/remote
            builder.setConnectionPoint(connectionPoints);
        } else {
            // Try local local
            connectionPoints = getVllLocalPoints(id, ctx, netName, isOper);
            if (connectionPoints.size() == 2) {
                builder.setConnectionPoint(connectionPoints);
            }
        }
    }

    private boolean isOper(ReadContext ctx) {
        Object flag = ctx.getModificationCache().get(Reader.DS_TYPE_FLAG);
        return flag != null && flag == LogicalDatastoreType.OPERATIONAL;
    }

    public static final String SH_VLL = "sh mpls vll %s";
    public static final Pattern VLL_PEER_LINE = Pattern.compile("Vll-Peer\\s+:\\s+(?<remoteIp>\\S+).*");
    public static final Pattern VLL_LOCAL_IFC_LINE = Pattern.compile("End-point[^:]+:\\s+untagged\\s+(?<ifc>.+)");
    public static final Pattern VLL_LOCAL_SUBIFC_LINE = Pattern.compile("End-point[^:]+:\\s+tagged\\s+vlan (?<vlan>[0-9]+)\\s+(?<ifc>.+)");
    public static final Pattern VLL_VCCID_LINE = Pattern.compile(".*VC-ID (?<vccid>\\S+),.*");

    public static final String SH_VLL_LOCAL = "sh mpls vll-local %s";

    private List<ConnectionPoint> getVllPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(String.format(SH_VLL, netName), this.cli, id, ctx);
        return parseVllPoints(output, isOper, id, ctx);
    }

    private List<ConnectionPoint> getVllLocalPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(String.format(SH_VLL_LOCAL, netName), this.cli, id, ctx);
        return parseVllLocalPoints(output, isOper, id, ctx);
    }

    private List<ConnectionPoint> parseVllPoints(String output, boolean isOper, InstanceIdentifier<ConnectionPoints> id, ReadContext ctx) throws ReadFailedException {
        Optional<String> remoteIp = parseField(output, 0,
                VLL_PEER_LINE::matcher,
                m -> m.group("remoteIp"));
        if (!remoteIp.isPresent()) {
            return Collections.emptyList();
        }

        Optional<String> vccId = parseField(output, 0,
                VLL_VCCID_LINE::matcher,
                m -> m.group("vccid"));
        if (!vccId.isPresent()) {
            return Collections.emptyList();
        }

        Optional<String> localIfc = parseField(output, 0,
                VLL_LOCAL_IFC_LINE::matcher,
                m -> m.group("ifc"));

        Optional<String> localSubifc = parseField(output, 0,
                VLL_LOCAL_SUBIFC_LINE::matcher,
                m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));
        if (!localIfc.isPresent() && !localSubifc.isPresent()) {
            return Collections.emptyList();
        }

        InterfaceId localIfcId = InterfaceId.parse(localIfc.orElseGet(localSubifc::get));
        localIfcId = expandIfcName(localIfcId, id, ctx);

        return extractVllPoints(localIfcId,
                vccId.get(),
                remoteIp.get(),
                isOper);
    }

    private List<ConnectionPoint> parseVllLocalPoints(String output, boolean isOper, InstanceIdentifier<ConnectionPoints> id, ReadContext ctx) throws ReadFailedException {
        Optional<String> ifc1 = extractVllLocalIfc(output, ENDPOINT_1_LINE, VLL_LOCAL_IFC_LINE,
                m -> m.group("ifc"));
        Optional<String> subifc1 = extractVllLocalIfc(output, ENDPOINT_1_LINE, VLL_LOCAL_SUBIFC_LINE,
                m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));
        if (!ifc1.isPresent() && !subifc1.isPresent()) {
            return Collections.emptyList();
        }

        Optional<String> ifc2 = extractVllLocalIfc(output, ENDPOINT_2_LINE, VLL_LOCAL_IFC_LINE,
                m -> m.group("ifc"));
        Optional<String> subifc2 = extractVllLocalIfc(output, ENDPOINT_2_LINE, VLL_LOCAL_SUBIFC_LINE,
                m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));
        if (!ifc2.isPresent() && !subifc2.isPresent()) {
            return Collections.emptyList();
        }

        InterfaceId ifcId1 = InterfaceId.parse(ifc1.orElseGet(subifc1::get));
        ifcId1 = expandIfcName(ifcId1, id, ctx);
        InterfaceId ifcId2 = InterfaceId.parse(ifc2.orElseGet(subifc2::get));
        ifcId2 = expandIfcName(ifcId2, id, ctx);

        return extractVllLocalPoints(ifcId1, ifcId2, isOper);
    }

    private InterfaceId expandIfcName(InterfaceId ifc, InstanceIdentifier<ConnectionPoints> id, ReadContext ctx) throws ReadFailedException {
        String output = blockingRead(String.format(SH_SINGLE_INTERFACE, ifc.toParentIfcString(), ""), cli, id, ctx);
        Optional<String> fullIfcName = ParsingUtils.parseField(output, 0,
                STATUS_LINE::matcher,
                m -> m.group("id"));

        checkArgument(fullIfcName.isPresent(), "Unknown interface %s", ifc);
        return new InterfaceId(fullIfcName.get(), ifc.subifc);
    }

    @VisibleForTesting
    static Optional<String> extractVllLocalIfc(String output,
                                               String endpointToMatch,
                                               Pattern matcher,
                                               Function<Matcher, String> extract) {
        return NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(line -> line.contains(endpointToMatch))
                .map(matcher::matcher)
                .filter(Matcher::matches)
                .map(extract)
                .findFirst();
    }

    private List<ConnectionPoint> extractVllLocalPoints(InterfaceId ifc1, InterfaceId ifc2, boolean isOper) {

        Local local = getLocal(isOper, ifc1);
        Local local2 = getLocal(isOper, ifc2);

        Endpoint localEndpoint = getEndpoint(isOper, LOCAL.class)
                .setLocal(local)
                .build();
        ConnectionPointBuilder point1Builder = getConnectionPointBuilder(isOper, localEndpoint, POINT_1);

        Endpoint localEndpoint2 = getEndpoint(isOper, LOCAL.class)
                .setLocal(local2)
                .build();
        ConnectionPointBuilder point2Builder = getConnectionPointBuilder(isOper, localEndpoint2, POINT_2);

        return Lists.newArrayList(point1Builder.build(), point2Builder.build());
    }

    private List<ConnectionPoint> extractVllPoints(InterfaceId ifc1, String vccId, String remoteIp,
                                                   boolean isOper) {
        Local local = getLocal(isOper, ifc1);

        IpAddress remoteIp1 = new IpAddress(new Ipv4Address(remoteIp));
        Long vccid = Long.valueOf(vccId);
        Remote remote = getXconnectRemote(isOper, remoteIp1, vccid);

        Endpoint localEndpoint = getEndpoint(isOper, LOCAL.class)
                .setLocal(local)
                .build();

        ConnectionPointBuilder point1Builder = getConnectionPointBuilder(isOper, localEndpoint, POINT_1);

        Endpoint remoteEndpoint = getEndpoint(isOper, REMOTE.class)
                .setRemote(remote)
                .build();

        ConnectionPointBuilder point2Builder = getConnectionPointBuilder(isOper, remoteEndpoint, POINT_2);

        return Lists.newArrayList(point1Builder.build(), point2Builder.build());
    }

    private ConnectionPointBuilder getConnectionPointBuilder(boolean isOper, Endpoint localEndpoint, String pointId) {
        ConnectionPointBuilder point1Builder = new ConnectionPointBuilder();
        point1Builder.setConnectionPointId(pointId)
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.ConfigBuilder()
                        .setConnectionPointId(pointId)
                        .build())
                .setEndpoints(new EndpointsBuilder()
                        .setEndpoint(Collections.singletonList(localEndpoint))
                        .build());
        if (isOper) {
            point1Builder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.StateBuilder()
                    .setConnectionPointId(pointId)
                    .build());
        }
        return point1Builder;
    }

    private static EndpointBuilder getEndpoint(boolean isOper, Class<? extends ENDPOINTTYPE> type) {
        EndpointBuilder localEndpointBuilder = new EndpointBuilder();
        localEndpointBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.ConfigBuilder()
                .setEndpointId(ENDPOINT_ID)
                .setPrecedence(0)
                .setType(type)
                .build());
        if (isOper) {
            localEndpointBuilder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.StateBuilder()
                    .setEndpointId(ENDPOINT_ID)
                    .setActive(true)
                    .setPrecedence(0)
                    .setType(type)
                    .build());
        }
        return localEndpointBuilder
                .setEndpointId(ENDPOINT_ID);
    }

    private static Remote getXconnectRemote(boolean isOper, IpAddress remoteIp, Long vccid) {
        RemoteBuilder remoteBuilder = new RemoteBuilder();
        remoteBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.remote.ConfigBuilder()
                .setRemoteSystem(remoteIp)
                .setVirtualCircuitIdentifier(vccid)
                .build());
        if (isOper) {
            remoteBuilder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.remote.StateBuilder()
                    .setRemoteSystem(remoteIp)
                    .setVirtualCircuitIdentifier(vccid)
                    .build());
        }
        return remoteBuilder.build();
    }

    private static Local getLocal(boolean isOper, InterfaceId ifcId) {
        LocalBuilder localBuilder = new LocalBuilder();
        localBuilder.setConfig(new ConfigBuilder()
                .setInterface(ifcId.ifc)
                .setSubinterface(ifcId.subifc)
                .build());
        if (isOper) {
            localBuilder.setState(new StateBuilder()
                    .setInterface(ifcId.ifc)
                    .setSubinterface(ifcId.subifc)
                    .build());
        }
        return localBuilder.build();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull ConnectionPoints readValue) {
        ((NetworkInstanceBuilder) parentBuilder).setConnectionPoints(readValue);
    }

    static final class InterfaceId {

        private final String ifc;
        private Long subifc;

        private InterfaceId(String ifcRealName, Long subifc) {
            this.ifc = ifcRealName;
            this.subifc = subifc;
        }

        private InterfaceId(String ifcRealName) {
            this.ifc = ifcRealName;
        }

        static InterfaceId parse(String name) {
            String[] splitIfcName = name.split("\\.");

            String ifcRealName = splitIfcName.length == 2 ? splitIfcName[0] : name;
            Long subifc = splitIfcName.length == 2 ? Long.valueOf(splitIfcName[1]) : null;

            return new InterfaceId(ifcRealName, subifc);
        }

        @Override
        public String toString() {
            String subifcString = subifc != null ? ("." + subifc) : "";
            return ifc + subifcString;
        }

        String toParentIfcString() {
            return ifc;
        }

        static InterfaceId fromEndpoint(Endpoint endpoint1) {
            return new InterfaceId(endpoint1.getLocal().getConfig().getInterface(),
                    ((Long) endpoint1.getLocal().getConfig().getSubinterface()));
        }
    }

}

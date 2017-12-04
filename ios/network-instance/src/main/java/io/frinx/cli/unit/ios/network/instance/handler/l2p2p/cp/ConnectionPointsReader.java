/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp;

import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader.LOCAL_CONNECT_ID_LINE;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader.SH_INTERFACES_XCONNECT;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader.SH_LOCAL_CONNECT;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader.XCONNECT_ID_LINE;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader.realignXconnectInterfacesOutput;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.read.Reader;
import io.frinx.cli.handlers.network.instance.L2p2pReader;
import io.frinx.cli.io.Cli;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
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
    public static final String POINT_2 = "2";
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
        List<ConnectionPoint> connectionPoints = getXconnectPoints(id, ctx, netName, isOper);

        if (connectionPoints.size() == 2) {
            // Try to set xconnect local/remote
            builder.setConnectionPoint(connectionPoints);
        } else {
            // Try local local
            connectionPoints = getLocalConnectPoints(id, ctx, netName, isOper);
            if (connectionPoints.size() == 2) {
                builder.setConnectionPoint(connectionPoints);
            }
        }
    }

    private boolean isOper(ReadContext ctx) {
        Object flag = ctx.getModificationCache().get(Reader.DS_TYPE_FLAG);
        return flag != null && flag == LogicalDatastoreType.OPERATIONAL;
    }

    private List<ConnectionPoint> getXconnectPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(SH_INTERFACES_XCONNECT, this.cli, id, ctx);
        return parseXconnectPoints(netName, output, isOper);
    }

    private List<ConnectionPoint> getLocalConnectPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(SH_LOCAL_CONNECT, this.cli, id, ctx);
        return parseLocalConnectPoints(netName, output, isOper);
    }

    private List<ConnectionPoint> parseXconnectPoints(String netName, String output, boolean isOper) {
        String linePerInterface = realignXconnectInterfacesOutput(output);

        return NEWLINE.splitAsStream(linePerInterface)
                .map(String::trim)
                .map(line -> line.replaceAll("\\s+", " "))
                .filter(line -> line.contains(netName))
                .map(XCONNECT_ID_LINE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(matcher -> extractXconnectPoints(matcher, isOper))
                .orElse(Collections.emptyList());
    }

    private List<ConnectionPoint> parseLocalConnectPoints(String netName, String output, boolean isOper) {
        String linePerInterface = realignXconnectInterfacesOutput(output);

        return NEWLINE.splitAsStream(linePerInterface)
                .map(String::trim)
                .filter(line -> line.contains(netName))
                .map(LOCAL_CONNECT_ID_LINE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(matcher -> extractLocalConnectPoints(matcher, isOper))
                .orElse(Collections.emptyList());
    }

    private List<ConnectionPoint> extractLocalConnectPoints(Matcher matcher, boolean isOper) {
        InterfaceId ifc1 = InterfaceId.parse(matcher.group("interface1"));
        InterfaceId ifc2 = InterfaceId.parse(matcher.group("interface2"));

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

    private List<ConnectionPoint> extractXconnectPoints(Matcher matcher, boolean isOper) {
        InterfaceId ifc1 = InterfaceId.parse(matcher.group("interface"));
        Local local = getLocal(isOper, ifc1);

        IpAddress remoteIp = new IpAddress(new Ipv4Address(matcher.group("ip")));
        Long vccid = Long.valueOf(matcher.group("vccid"));
        Remote remote = getXconnectRemote(isOper, remoteIp, vccid);

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

        private String ifc;
        private Long subifc;

        private InterfaceId(String ifcRealName, Long subifc) {
            this.ifc = ifcRealName;
            this.subifc = subifc;
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

        public String toParentIfcString() {
            return ifc;
        }

        static InterfaceId fromEndpoint(Endpoint endpoint1) {
            return new InterfaceId(endpoint1.getLocal().getConfig().getInterface(),
                    ((Long) endpoint1.getLocal().getConfig().getSubinterface()));
        }
    }

}

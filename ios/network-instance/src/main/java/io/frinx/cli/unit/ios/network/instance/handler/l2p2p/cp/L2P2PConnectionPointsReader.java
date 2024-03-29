/*
 * Copyright © 2018 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.read.Reader;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.L2P2PReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PConnectionPointsReader implements CliConfigReader<ConnectionPoints, ConnectionPointsBuilder>,
        CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder> {

    public static final String POINT_1 = "1";
    public static final String POINT_2 = "2";
    public static final String ENDPOINT_ID = "default";

    private final Cli cli;

    public L2P2PConnectionPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                             @NotNull ConnectionPointsBuilder builder,
                                             @NotNull ReadContext ctx) throws ReadFailedException {
        boolean isOper = isOper(ctx);
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
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
        Object flag = ctx.getModificationCache()
                .get(Reader.DS_TYPE_FLAG);
        return flag != null && flag == LogicalDatastoreType.OPERATIONAL;
    }

    private List<ConnectionPoint> getXconnectPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String
            netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(L2P2PReader.SH_INTERFACES_XCONNECT, this.cli, id, ctx);
        return parseXconnectPoints(netName, output, isOper);
    }

    private List<ConnectionPoint> getLocalConnectPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx,
                                                        String netName, boolean isOper)
            throws ReadFailedException {
        String output = blockingRead(L2P2PReader.SH_LOCAL_CONNECT, this.cli, id, ctx);
        return parseLocalConnectPoints(netName, output, isOper);
    }

    private List<ConnectionPoint> parseXconnectPoints(String netName, String output, boolean isOper) {
        String linePerInterface = L2P2PReader.realignXconnectInterfacesOutput(output);

        return ParsingUtils.NEWLINE.splitAsStream(linePerInterface)
                .map(String::trim)
                .map(line -> line.replaceAll("\\s+", " "))
                .filter(line -> line.contains(netName))
                .map(L2P2PReader.XCONNECT_ID_LINE::matcher)
                .filter(Matcher::matches)
                .findFirst()
                .map(matcher -> extractXconnectPoints(matcher, isOper))
                .orElse(Collections.emptyList());
    }

    private List<ConnectionPoint> parseLocalConnectPoints(String netName, String output, boolean isOper) {
        String linePerInterface = L2P2PReader.realignXconnectInterfacesOutput(output);

        return ParsingUtils.NEWLINE.splitAsStream(linePerInterface)
                .map(String::trim)
                .filter(line -> line.contains(netName))
                .map(L2P2PReader.LOCAL_CONNECT_ID_LINE::matcher)
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
        Remote remote = getRemote(isOper, remoteIp, vccid);

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

    public static ConnectionPointBuilder getConnectionPointBuilder(boolean isOper, Endpoint localEndpoint, String
            pointId) {
        ConnectionPointBuilder point1Builder = new ConnectionPointBuilder();
        point1Builder.setConnectionPointId(pointId)
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                        .rev170228.network.instance.top.network.instances.network.instance.connection.points
                        .connection.point.ConfigBuilder()
                        .setConnectionPointId(pointId)
                        .build())
                .setEndpoints(new EndpointsBuilder()
                        .setEndpoint(Collections.singletonList(localEndpoint))
                        .build());
        if (isOper) {
            point1Builder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                    .rev170228.network.instance.top.network.instances.network.instance.connection.points.connection
                    .point.StateBuilder()
                    .setConnectionPointId(pointId)
                    .build());
        }
        return point1Builder;
    }

    public static EndpointBuilder getEndpoint(boolean isOper, Class<? extends ENDPOINTTYPE> type) {
        EndpointBuilder localEndpointBuilder = new EndpointBuilder();
        localEndpointBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network
                .instance.rev170228.network.instance.top.network.instances.network.instance.connection.points
                .connection.point.endpoints.endpoint.ConfigBuilder()
                .setEndpointId(ENDPOINT_ID)
                .setPrecedence(0)
                .setType(type)
                .build());
        if (isOper) {
            localEndpointBuilder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network
                    .instance.rev170228.network.instance.top.network.instances.network.instance.connection.points
                    .connection.point.endpoints.endpoint.StateBuilder()
                    .setEndpointId(ENDPOINT_ID)
                    .setActive(true)
                    .setPrecedence(0)
                    .setType(type)
                    .build());
        }
        return localEndpointBuilder
                .setEndpointId(ENDPOINT_ID);
    }

    public static Remote getRemote(boolean isOper, IpAddress remoteIp, Long vccid) {
        RemoteBuilder remoteBuilder = new RemoteBuilder();
        remoteBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                .rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point
                .endpoints.endpoint.remote.ConfigBuilder()
                .setRemoteSystem(remoteIp)
                .setVirtualCircuitIdentifier(vccid)
                .build());
        if (isOper) {
            remoteBuilder.setState(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                    .rev170228.network.instance.top.network.instances.network.instance.connection.points.connection
                    .point.endpoints.endpoint.remote.StateBuilder()
                    .setRemoteSystem(remoteIp)
                    .setVirtualCircuitIdentifier(vccid)
                    .build());
        }
        return remoteBuilder.build();
    }

    public static Local getLocal(boolean isOper, InterfaceId ifcId) {
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
    public Check getCheck() {
        return BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P);
    }

    public static final class InterfaceId {

        private String ifc;
        private Long subifc;

        private InterfaceId(String ifcRealName, Long subifc) {
            this.ifc = ifcRealName;
            this.subifc = subifc;
        }

        public static InterfaceId parse(String name) {
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

        public static InterfaceId fromEndpoint(Endpoint endpoint1) {
            return new InterfaceId(endpoint1.getLocal()
                    .getConfig()
                    .getInterface(),
                    ((Long) endpoint1.getLocal()
                            .getConfig()
                            .getSubinterface()));
        }
    }
}
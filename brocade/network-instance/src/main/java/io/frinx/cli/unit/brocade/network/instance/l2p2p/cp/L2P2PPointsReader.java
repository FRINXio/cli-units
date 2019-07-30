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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.ENDPOINTTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@SuppressWarnings("Indentation")
public class L2P2PPointsReader implements CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder>,
        CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    public static final String POINT_1 = "1";
    public static final String POINT_2 = "2";
    public static final String ENDPOINT_ID = "default";
    public static final Pattern TAGGED_IFC_PREFIX_PATTERN = Pattern.compile("\n\\s+tag(ged)?");

    private final Cli cli;

    public L2P2PPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                      @Nonnull ConnectionPointsBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        List<ConnectionPoint> connectionPoints = getVllPoints(id, ctx, netName);

        if (connectionPoints.size() == 2) {
            // Try to set xconnect local/remote
            builder.setConnectionPoint(connectionPoints);
        } else {
            // Try local local
            connectionPoints = getVllLocalPoints(id, ctx, netName);
            if (connectionPoints.size() == 2) {
                builder.setConnectionPoint(connectionPoints);
            }
        }
    }

    static final String SH_VLL = "show running-config | begin  vll %s";
    static final Pattern VLL_VCCID_LINE = Pattern.compile("vll (?<name>\\S+)\\s+(?<vccid>\\d+)");
    static final Pattern VLL_PEER_LINE = Pattern.compile("vll-peer\\s+(?<remoteIp>\\S+)");
    static final Pattern VLL_LOCAL_IFC_LINE = Pattern.compile("untag(ged)?\\s+(?<ifc>.+)");
    static final Pattern VLL_LOCAL_SUBIFC_LINE = Pattern.compile("vlan (?<vlan>[0-9]+)\\s+tag(ged)? (?<ifc>.+)");

    static final String SH_VLL_LOCAL = "show running-config | begin  vll-local %s";
    static final Pattern VLL_2_LOCAL_SUBIFC_LINE =
            Pattern.compile("vlan (?<vlan>[0-9]+)\\s+tag(ged)? (?<ifc>.+)\\s+tag(ged)? (?<ifc2>.+)");

    private List<ConnectionPoint> getVllPoints(InstanceIdentifier<ConnectionPoints> id,
                                               ReadContext ctx,
                                               String netName) throws ReadFailedException {
        String output = blockingRead(String.format(SH_VLL, netName), this.cli, id, ctx);
        if (output.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return parseVllPoints(output);
        }
    }

    private List<ConnectionPoint> getVllLocalPoints(InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, String
            netName) throws ReadFailedException {
        String output = blockingRead(String.format(SH_VLL_LOCAL, netName), this.cli, id, ctx);
        if (output.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return parseVllLocalPoints(output);
        }
    }

    @VisibleForTesting
    static List<ConnectionPoint> parseVllPoints(String output) {

        int endIdx = output.indexOf(Cli.NEWLINE + Cli.NEWLINE);
        if (endIdx == -1) {
            endIdx = output.indexOf(Cli.NEWLINE + "\r" + Cli.NEWLINE);
        }
        output = output.substring(0, endIdx);
        output = TAGGED_IFC_PREFIX_PATTERN.matcher(output).replaceAll(" tagged");

        Optional<String> remoteIp = ParsingUtils.parseField(output, 0, VLL_PEER_LINE::matcher,
                m -> m.group("remoteIp"));
        if (!remoteIp.isPresent()) {
            return Collections.emptyList();
        }

        Optional<String> vccId = ParsingUtils.parseField(output, 0,
                VLL_VCCID_LINE::matcher,
                m -> m.group("vccid"));
        if (!vccId.isPresent()) {
            return Collections.emptyList();
        }

        Optional<String> localIfc = ParsingUtils.parseField(output, 0,
                VLL_LOCAL_IFC_LINE::matcher,
                m -> m.group("ifc"));

        Optional<String> localSubifc = ParsingUtils.parseField(output, 0,
                VLL_LOCAL_SUBIFC_LINE::matcher,
                m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));
        if (!localIfc.isPresent() && !localSubifc.isPresent()) {
            return Collections.emptyList();
        }

        InterfaceId localIfcId = InterfaceId.parse(localIfc.orElseGet(localSubifc::get));

        return extractVllPoints(localIfcId,
                vccId.get(),
                remoteIp.get());
    }

    @VisibleForTesting
    static List<ConnectionPoint> parseVllLocalPoints(String output) {

        int endIdx = output.indexOf(Cli.NEWLINE + Cli.NEWLINE);
        if (endIdx == -1) {
            endIdx = output.indexOf(Cli.NEWLINE + "\r" + Cli.NEWLINE);
        }
        output = output.substring(0, endIdx);
        output = TAGGED_IFC_PREFIX_PATTERN.matcher(output).replaceAll(" tagged");

        List<String> ifcs = ParsingUtils.parseFields(output, 0,
                VLL_LOCAL_IFC_LINE::matcher,
                m -> m.group("ifc"),
                Function.identity());

        if (ifcs.size() == 0) {
            // 2 tagged subinterfaces
            Optional<String> subinterface1 = ParsingUtils.parseField(output, 0, VLL_2_LOCAL_SUBIFC_LINE::matcher,
                    m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));
            Optional<String> subinterface2 = ParsingUtils.parseField(output, 0, VLL_2_LOCAL_SUBIFC_LINE::matcher,
                    m -> String.format("%s.%s", m.group("ifc2"), m.group("vlan")));

            if (subinterface1.isPresent() && subinterface2.isPresent()) {
                // 2 tagged subinterfaces from the same vlan
                InterfaceId p1 = InterfaceId.parse(subinterface1.get());
                InterfaceId p2 = InterfaceId.parse(subinterface2.get());
                return extractVllLocalPoints(p1, p2);
            } else if (!subinterface1.isPresent() && !subinterface2.isPresent()) {
                // 2 tagged subinterfaces from the different vlans
                List<String> subifcs = ParsingUtils.parseFields(output, 0, VLL_LOCAL_SUBIFC_LINE::matcher,
                        m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")), Function.identity());

                if (subifcs.size() == 2) {
                    InterfaceId p1 = InterfaceId.parse(subifcs.get(0));
                    InterfaceId p2 = InterfaceId.parse(subifcs.get(1));
                    return extractVllLocalPoints(p1, p2);
                } else {
                    // incomplete config on device
                    return Collections.emptyList();
                }
            } else {
                // incomplete config on device
                return Collections.emptyList();
            }

        } else if (ifcs.size() == 1) {
            // 1 untagged interface and 1 tagged subinterface
            InterfaceId p1 = InterfaceId.parse(ifcs.get(0));
            Optional<String> subinterface = ParsingUtils.parseField(output, 0, VLL_LOCAL_SUBIFC_LINE::matcher,
                    m -> String.format("%s.%s", m.group("ifc"), m.group("vlan")));

            if (!subinterface.isPresent()) {
                // incomplete config on device
                return Collections.emptyList();
            }

            InterfaceId p2 = InterfaceId.parse(subinterface.get());

            return output.indexOf("untag") < output.indexOf("tag")
                    ? extractVllLocalPoints(p1, p2)
                    : extractVllLocalPoints(p2, p1);
        } else if (ifcs.size() == 2) {
            // 2 untagged interfaces
            InterfaceId p1 = InterfaceId.parse(ifcs.get(0));
            InterfaceId p2 = InterfaceId.parse(ifcs.get(1));
            return extractVllLocalPoints(p1, p2);
        } else {
            // incomplete config on device
            return Collections.emptyList();
        }
    }

    static List<ConnectionPoint> extractVllLocalPoints(InterfaceId ifc1, InterfaceId ifc2) {

        Local local = getLocal(ifc1);
        Local local2 = getLocal(ifc2);

        Endpoint localEndpoint = getEndpoint(LOCAL.class)
                .setLocal(local)
                .build();
        ConnectionPointBuilder point1Builder = getConnectionPointBuilder(localEndpoint, POINT_1);

        Endpoint localEndpoint2 = getEndpoint(LOCAL.class)
                .setLocal(local2)
                .build();
        ConnectionPointBuilder point2Builder = getConnectionPointBuilder(localEndpoint2, POINT_2);

        return Lists.newArrayList(point1Builder.build(), point2Builder.build());
    }

    private static List<ConnectionPoint> extractVllPoints(InterfaceId ifc1, String vccId, String remoteIp) {
        Local local = getLocal(ifc1);

        IpAddress remoteIp1 = new IpAddress(new Ipv4Address(remoteIp));
        Long vccid = Long.valueOf(vccId);
        Remote remote = getXconnectRemote(remoteIp1, vccid);

        Endpoint localEndpoint = getEndpoint(LOCAL.class)
                .setLocal(local)
                .build();

        ConnectionPointBuilder point1Builder = getConnectionPointBuilder(localEndpoint, POINT_1);

        Endpoint remoteEndpoint = getEndpoint(REMOTE.class)
                .setRemote(remote)
                .build();

        ConnectionPointBuilder point2Builder = getConnectionPointBuilder(remoteEndpoint, POINT_2);

        return Lists.newArrayList(point1Builder.build(), point2Builder.build());
    }

    private static ConnectionPointBuilder getConnectionPointBuilder(Endpoint localEndpoint, String pointId) {
        ConnectionPointBuilder point1Builder = new ConnectionPointBuilder();
        point1Builder.setConnectionPointId(pointId).setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                .net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance
                .connection.points.connection.point.ConfigBuilder().setConnectionPointId(pointId).build())
                .setEndpoints(new EndpointsBuilder().setEndpoint(Collections.singletonList(localEndpoint)).build());
        return point1Builder;
    }

    private static EndpointBuilder getEndpoint(Class<? extends ENDPOINTTYPE> type) {
        EndpointBuilder localEndpointBuilder = new EndpointBuilder();
        localEndpointBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network
                .instance.rev170228.network.instance.top.network.instances.network.instance.connection.points
                .connection.point.endpoints.endpoint.ConfigBuilder().setEndpointId(ENDPOINT_ID).setPrecedence(0)
                .setType(type).build());
        return localEndpointBuilder.setEndpointId(ENDPOINT_ID);
    }

    private static Remote getXconnectRemote(IpAddress remoteIp, Long vccid) {
        RemoteBuilder remoteBuilder = new RemoteBuilder();
        remoteBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                .rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point
                .endpoints.endpoint.remote.ConfigBuilder().setRemoteSystem(remoteIp).setVirtualCircuitIdentifier(vccid)
                .build());
        return remoteBuilder.build();
    }

    private static Local getLocal(InterfaceId ifcId) {
        LocalBuilder localBuilder = new LocalBuilder();
        localBuilder.setConfig(new ConfigBuilder()
                .setInterface(ifcId.ifc)
                .setSubinterface(ifcId.subifc)
                .build());
        return localBuilder.build();
    }

    @Override
    public Check getCheck() {
        return BasicCheck.checkData(
                ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P);
    }

    public static final class InterfaceId {

        public final String ifc;
        public final Long subifc;

        public InterfaceId(String ifcName, Long subifc) {
            this.ifc = Util.expandInterfaceName(ifcName);
            this.subifc = subifc;
        }

        private InterfaceId(String ifcName) {
            this(ifcName, null);
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
            return new InterfaceId(endpoint1.getLocal().getConfig().getInterface(),
                    ((Long) endpoint1.getLocal().getConfig().getSubinterface()));
        }
    }

}

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

package io.frinx.cli.unit.brocade.network.instance.l2vsi.cp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.brocade.ifc.handler.switchedvlan.def.Vlan;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.cp.L2P2PPointsReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.NiCpBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.NiCpBrocadeAugBuilder;
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
public class L2VSIPointsReader implements CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder>,
        CliConfigReader<ConnectionPoints, ConnectionPointsBuilder> {

    private final Cli cli;

    public L2VSIPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                      @Nonnull ConnectionPointsBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String vplsName = id.firstKeyOf(NetworkInstance.class).getName();
        List<ConnectionPoint> connectionPoints = getConnectionPoints(vplsName, id, ctx);
        if (!connectionPoints.isEmpty()) {
            builder.setConnectionPoint(connectionPoints);
        }
    }

    private List<ConnectionPoint> getConnectionPoints(String vplsName, InstanceIdentifier<ConnectionPoints> id,
                                                      ReadContext ctx) throws ReadFailedException {

        String output = blockingRead(f(SH_VPLS, vplsName), this.cli, id, ctx);
        return new ArrayList<>(getVplsPoints(output));
    }

    private static final String SH_VPLS = "show running-config | begin ^ vpls %s";
    private static final Pattern VPLS_VCCID_LINE = Pattern.compile("vpls (?<name>\\S+)\\s+(?<vccid>\\d+)");
    private static final Pattern VPLS_PEERS_LINE = Pattern.compile("vpls-peer\\s+(?<remoteIps>.*)");
    private static final Pattern VPLS_PEER_DIVIDER = Pattern.compile(" ");
    private static final Pattern VPLS_SUBIFCS_DIVIDER = Pattern.compile(" \n {3}");
    private static final String UNTAGGED_IFC = "untagged";

    private List<ConnectionPoint> getVplsPoints(String output) {
        if (output.trim().isEmpty()) {
            return Collections.emptyList();
        } else {
            return parseVplsLines(output);
        }
    }

    @VisibleForTesting
    static List<ConnectionPoint> parseVplsLines(String output) {

        int endIdx = output.indexOf(Cli.NEWLINE + Cli.NEWLINE);
        if (endIdx == -1) {
            endIdx = output.indexOf(Cli.NEWLINE + "\r" + Cli.NEWLINE);
        }
        output = output.substring(0, endIdx);

        List<String> remoteIps = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(VPLS_PEERS_LINE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("remoteIps"))
                .collect(Collectors.toList());
        if (remoteIps.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<String> vccId = ParsingUtils.parseField(output, 0,
                VPLS_VCCID_LINE::matcher,
                m -> m.group("vccid"));
        if (!vccId.isPresent()) {
            return Collections.emptyList();
        }

        Vlan vlan = Vlan.create(VPLS_SUBIFCS_DIVIDER.matcher(output).replaceAll(" "));
        if (vlan == null || vlan.getInterfaces().isEmpty()) {
            return Collections.emptyList();
        }

        return extractVplsPoints(vlan, vccId.get(), remoteIps);
    }

    private static List<ConnectionPoint> extractVplsPoints(Vlan vlan, String vccId, List<String> remoteIp) {

        AtomicInteger cpId = new AtomicInteger(1);
        List<ConnectionPoint> locals = vlan.getInterfaces().stream()
                .flatMap(ifc -> ifc.getInterfaces().stream()
                    .map(local -> L2VSIPointsReader.getLocal(local, vlan.getId().getValue(), ifc.getTag()))
                    .map(local -> getEndpoint(LOCAL.class).setLocal(local).build())
                    .map(ep -> getConnectionPointBuilder(ep, String.valueOf(cpId.getAndIncrement())).build()))
                .collect(Collectors.toList());

        Long vccid = Long.valueOf(vccId);
        List<ConnectionPoint> remotes = remoteIp.stream()
                .flatMap(VPLS_PEER_DIVIDER::splitAsStream)
                .map(Ipv4Address::new)
                .map(IpAddress::new)
                .map(remIp -> getRemote(remIp, vccid))
                .map(remote -> getEndpoint(REMOTE.class).setRemote(remote).build())
                .map(ep -> getConnectionPointBuilder(ep, String.valueOf(cpId.getAndIncrement())).build())
                .collect(Collectors.toList());

        ArrayList<ConnectionPoint> connectionPoints = Lists.newArrayList();
        connectionPoints.addAll(locals);
        connectionPoints.addAll(remotes);
        return connectionPoints;
    }

    private static ConnectionPointBuilder getConnectionPointBuilder(Endpoint endpoint, String pointId) {
        ConnectionPointBuilder point1Builder = new ConnectionPointBuilder();
        point1Builder.setConnectionPointId(pointId).setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig
                .net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance
                .connection.points.connection.point.ConfigBuilder().setConnectionPointId(pointId).build())
                .setEndpoints(new EndpointsBuilder().setEndpoint(Collections.singletonList(endpoint)).build());
        return point1Builder;
    }

    private static EndpointBuilder getEndpoint(Class<? extends ENDPOINTTYPE> type) {
        EndpointBuilder localEndpointBuilder = new EndpointBuilder();
        localEndpointBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network
                .instance.rev170228.network.instance.top.network.instances.network.instance.connection.points
                .connection.point.endpoints.endpoint.ConfigBuilder().setEndpointId(L2P2PPointsReader.ENDPOINT_ID)
                .setPrecedence(0).setType(type).build());
        localEndpointBuilder.setEndpointId(L2P2PPointsReader.ENDPOINT_ID);
        return localEndpointBuilder;
    }

    private static Remote getRemote(IpAddress remoteIp, Long vccid) {
        RemoteBuilder remoteBuilder = new RemoteBuilder();
        remoteBuilder.setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                .rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point
                .endpoints.endpoint.remote.ConfigBuilder().setRemoteSystem(remoteIp).setVirtualCircuitIdentifier(vccid)
                .build());
        return remoteBuilder.build();
    }

    private static Local getLocal(String ifcId, Integer subIfcId, String tag) {
        LocalBuilder localBuilder = new LocalBuilder();
        ConfigBuilder configBuilder = new ConfigBuilder();
        boolean isSubIfcUntgd = UNTAGGED_IFC.equals(tag);
        if (isSubIfcUntgd) {
            configBuilder.addAugmentation(NiCpBrocadeAug.class, new NiCpBrocadeAugBuilder()
                .setSubinterfaceUntagged(isSubIfcUntgd)
                .build());
        }
        localBuilder.setConfig(configBuilder
                .setInterface(ifcId)
                .setSubinterface(subIfcId.longValue())
                .build());
        return localBuilder.build();
    }

    @Override
    public Check getCheck() {
        return BasicCheck.checkData(
                ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2VSI);
    }

    public static final class InterfaceId {

        private final String ifc;
        private final Long subifc;

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

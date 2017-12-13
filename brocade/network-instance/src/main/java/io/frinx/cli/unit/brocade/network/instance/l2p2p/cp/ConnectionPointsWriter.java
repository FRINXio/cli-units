/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L2p2pWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2P2P;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ConnectionPointsWriter implements L2p2pWriter<ConnectionPoints> {

    private Cli cli;

    public ConnectionPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                              @Nonnull ConnectionPoints dataAfter,
                                              @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkArgument(dataAfter.getConnectionPoint().size() == 2,
                "L2P2P network only supports 2 endpoints, but were: %s", dataAfter.getConnectionPoint());

        Set<String> usedInterfaces = getUsedInterfaces(id, writeContext);

        ConnectionPoint connectionPoint1 = getCPoint(dataAfter, ConnectionPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, usedInterfaces, true);

        ConnectionPoint connectionPoint2 = getCPoint(dataAfter, ConnectionPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, usedInterfaces, true);

        if (isLocal(endpoint1, endpoint2)) {
            writeVllLocal(id, dataAfter, endpoint1, endpoint2, writeContext);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            writeVll(id, dataAfter, endpoint1, endpoint2, writeContext);
        } else {
            throw new IllegalArgumentException("Unable to configure L2P2P with REMOTE only endpoints: " + dataAfter.getConnectionPoint());
        }
    }

    private static Set<String> getUsedInterfaces(@Nonnull InstanceIdentifier<ConnectionPoints> id, @Nonnull WriteContext writeContext) {
        return writeContext.readAfter(io.frinx.openconfig.openconfig.network.instance.IIDs.NETWORKINSTANCES).get()
                .getNetworkInstance().stream()
                .filter(instance -> instance.getConfig().getType() == L2P2P.class)
                .filter(instance -> !Objects.equals(instance.getName(), id.firstKeyOf(NetworkInstance.class).getName()))
                // Stream connection points
                .filter(instance -> instance.getConnectionPoints() != null && !instance.getConnectionPoints().getConnectionPoint().isEmpty())
                .flatMap(instance -> instance.getConnectionPoints().getConnectionPoint().stream())
                // Stream endpoints
                .filter(connPoint -> connPoint.getEndpoints() != null && !connPoint.getEndpoints().getEndpoint().isEmpty())
                .flatMap(connPoint -> connPoint.getEndpoints().getEndpoint().stream())
                .filter(endpoint -> endpoint.getConfig().getType() == LOCAL.class)
                .map(ConnectionPointsReader.InterfaceId::fromEndpoint)
                .map(ConnectionPointsReader.InterfaceId::toString)
                .collect(Collectors.toSet());
    }

    private void writeVll(InstanceIdentifier<ConnectionPoints> id,
                          ConnectionPoints dataAfter,
                          Endpoint endpoint1,
                          Endpoint endpoint2,
                          WriteContext writeContext) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        Endpoint local = getLocal(endpoint1, endpoint2);
        ConnectionPointsReader.InterfaceId ifc1 = ConnectionPointsReader.InterfaceId.fromEndpoint(local);
        Endpoint remote = getRemote(endpoint1, endpoint2);

        Interface interfaceData = getInterfaceData(writeContext, true, ifc1);
        String ifcType = InterfaceConfigReader.getTypeOnDevice(interfaceData.getConfig().getType());
        String ifcNumber = InterfaceConfigReader.getIfcNumber(ifc1.toParentIfcString());

        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                "router mpls",
                f("vll %s %s", netName, remote.getRemote().getConfig().getVirtualCircuitIdentifier()),
                f("vll-peer %s", remote.getRemote().getConfig().getRemoteSystem().getIpv4Address().getValue()),
                "end");

        if (local.getLocal().getConfig().getSubinterface() == null) {
            // With subifc
            blockingWriteAndRead(cli, id, dataAfter,
                    "conf t",
                    "router mpls",
                    f("vll %s %s", netName, remote.getRemote().getConfig().getVirtualCircuitIdentifier()),
                    f("untag %s %s", ifcType, ifcNumber),
                    "end");
        } else {
            // Without subifc
            blockingWriteAndRead(cli, id, dataAfter,
                    "conf t",
                    "router mpls",
                    f("vll %s %s", netName, remote.getRemote().getConfig().getVirtualCircuitIdentifier()),
                    f("vlan %s", local.getLocal().getConfig().getSubinterface()),
                    f("tag %s %s", ifcType, ifcNumber),
                    "end");
        }
    }

    private Endpoint getRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == REMOTE.class ? endpoint1 : endpoint2;
    }

    private Endpoint getLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == LOCAL.class ? endpoint1 : endpoint2;
    }

    private void deleteVll(InstanceIdentifier<ConnectionPoints> id,
                           Endpoint endpoint1, Endpoint endpoint2) throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        Endpoint remote = getRemote(endpoint1, endpoint2);

        blockingDeleteAndRead(cli, id,
                "conf t",
                "router mpls",
                f("no vll %s %s", netName, remote.getRemote().getConfig().getVirtualCircuitIdentifier()),
                "end");
    }

    private void writeVllLocal(InstanceIdentifier<ConnectionPoints> id,
                               ConnectionPoints dataAfter,
                               Endpoint endpoint1,
                               Endpoint endpoint2,
                               WriteContext writeContext) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        writeVllLocalEndpoint(id, dataAfter, endpoint1, netName, writeContext);
        writeVllLocalEndpoint(id, dataAfter, endpoint2, netName, writeContext);
    }

    private void writeVllLocalEndpoint(InstanceIdentifier<ConnectionPoints> id,
                                       ConnectionPoints dataAfter,
                                       Endpoint endpoint,
                                       String netName,
                                       WriteContext writeContext) throws WriteFailedException.CreateFailedException {

        ConnectionPointsReader.InterfaceId ifc = ConnectionPointsReader.InterfaceId.fromEndpoint(endpoint);

        Interface interfaceData = getInterfaceData(writeContext, true, ifc);
        String ifcType = InterfaceConfigReader.getTypeOnDevice(interfaceData.getConfig().getType());
        String ifcNumber = InterfaceConfigReader.getIfcNumber(ifc.toParentIfcString());

        if (endpoint.getLocal().getConfig().getSubinterface() == null) {
            // With subifc
            blockingWriteAndRead(cli, id, dataAfter,
                    "conf t",
                    "router mpls",
                    f("vll-local %s", netName),
                    f("untag %s %s", ifcType, ifcNumber),
                    "end");
        } else {
            // Without subifc
            blockingWriteAndRead(cli, id, dataAfter,
                    "conf t",
                    "router mpls",
                    f("vll-local %s", netName),
                    f("vlan %s", endpoint.getLocal().getConfig().getSubinterface()),
                    f("tag %s %s", ifcType, ifcNumber),
                    "end");
        }
    }

    private void deleteVllLocal(InstanceIdentifier<ConnectionPoints> id) throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(cli, id,
                "conf t",
                "router mpls",
                f("no vll-local %s", netName),
                "end");
    }

    private static Endpoint getEndpoint(ConnectionPoint connectionPoint1, WriteContext writeContext, Set<String> usedInterfaces, boolean isWrite) {
        Endpoint endpoint1 = connectionPoint1.getEndpoints().getEndpoint().get(0);
        checkArgument(endpoint1.getEndpointId().equals(ConnectionPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s", ConnectionPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal().getConfig() != null) {

            ConnectionPointsReader.InterfaceId interfaceId = ConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);

            getInterfaceData(writeContext, isWrite, interfaceId);

            // Check interface not used already (or its parent)
            checkArgument(!usedInterfaces.contains(interfaceId.toString()),
                    "Interface %s already used in L2P2P network as: %s", interfaceId, interfaceId);
            checkArgument(!usedInterfaces.contains(interfaceId.toParentIfcString()),
                    "Interface %s already used in L2P2P network as: %s", interfaceId, interfaceId.toParentIfcString());

            // No subinterface verification required here
        }
        return endpoint1;
    }

    private static Interface getInterfaceData(WriteContext writeContext, boolean isWrite, ConnectionPointsReader.InterfaceId ifcName) {
        Optional<Interface> ifcData = isWrite ?
                writeContext.readAfter(IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName.toParentIfcString()))) :
                writeContext.readBefore(IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName.toParentIfcString())));

        checkArgument(ifcData.isPresent(), "Unknown interface %s, cannot configure l2p2p", ifcName);

        return ifcData.get();
    }

    private static ConnectionPoint getCPoint(@Nonnull ConnectionPoints dataAfter, String expectedPointId) {
        ConnectionPoint connectionPoint = dataAfter.getConnectionPoint().stream()
                .filter(cp -> cp.getKey().getConnectionPointId().equals(expectedPointId))
                .findFirst()
                .get();

        checkArgument(connectionPoint.getConnectionPointId().equals(expectedPointId),
                "Connection point has to be named: %s, not %s", expectedPointId, connectionPoint.getConfig());
        checkArgument(connectionPoint.getEndpoints().getEndpoint().size() == 1,
                "Connection point must contain only 1 endpoint, but has: %s", connectionPoint.getEndpoints().getEndpoint());
        return connectionPoint;
    }

    private static boolean isLocalRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() != endpoint2.getConfig().getType();
    }

    private static boolean isLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == LOCAL.class &&
                endpoint2.getConfig().getType() == LOCAL.class;
    }

    @Override
    public void deleteCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                               @Nonnull ConnectionPoints dataBefore,
                                               @Nonnull WriteContext writeContext) throws WriteFailedException.DeleteFailedException {
        ConnectionPoint connectionPoint1 = getCPoint(dataBefore, ConnectionPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, Collections.emptySet(), false);

        ConnectionPoint connectionPoint2 = getCPoint(dataBefore, ConnectionPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, Collections.emptySet(), false);

        if (isLocal(endpoint1, endpoint2)) {
            deleteVllLocal(id);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            deleteVll(id, endpoint1, endpoint2);
        }
    }

    @Override
    public void updateCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                               @Nonnull ConnectionPoints dataBefore,
                                               @Nonnull ConnectionPoints dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}

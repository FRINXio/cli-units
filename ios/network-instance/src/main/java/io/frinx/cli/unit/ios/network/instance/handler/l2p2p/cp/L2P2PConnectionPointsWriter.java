/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L2p2pWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2P2P;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class L2P2PConnectionPointsWriter implements L2p2pWriter<ConnectionPoints> {

    private Cli cli;

    public L2P2PConnectionPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                              @Nonnull ConnectionPoints dataAfter,
                                              @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkArgument(dataAfter.getConnectionPoint().size() == 2,
                "L2P2P network only supports 2 endpoints, but were: %s", dataAfter.getConnectionPoint());

        Set<String> usedInterfaces = getUsedInterfaces(id, writeContext);

        ConnectionPoint connectionPoint1 = getCPoint(dataAfter, L2P2PConnectionPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, usedInterfaces, true, true);

        ConnectionPoint connectionPoint2 = getCPoint(dataAfter, L2P2PConnectionPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, usedInterfaces, true, true);

        if (isLocal(endpoint1, endpoint2)) {
            writeLocalConnect(id, dataAfter, endpoint1, endpoint2);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            writeXconnect(id, dataAfter, endpoint1, endpoint2);
        } else {
            throw new IllegalArgumentException("Unable to configure L2P2P with REMOTE only endpoints: " + dataAfter.getConnectionPoint());
        }
    }

    public static Set<String> getUsedInterfaces(@Nonnull InstanceIdentifier<ConnectionPoints> id, @Nonnull WriteContext writeContext) {
        return writeContext.readAfter(io.frinx.openconfig.openconfig.network.instance.IIDs.NETWORKINSTANCES).get()
                .getNetworkInstance()
                .stream()
                .filter(instance -> instance.getConfig().getType() == L2P2P.class || instance.getConfig().getType() == L2VSI.class)
                .filter(instance -> !Objects.equals(instance.getName(), id.firstKeyOf(NetworkInstance.class).getName()))
                .flatMap(instance -> instance.getConnectionPoints().getConnectionPoint().stream())
                .flatMap(connPoint -> connPoint.getEndpoints().getEndpoint().stream())
                .filter(endpoint -> endpoint.getConfig().getType() == LOCAL.class)
                .map(L2P2PConnectionPointsReader.InterfaceId::fromEndpoint)
                .map(L2P2PConnectionPointsReader.InterfaceId::toString)
                .collect(Collectors.toSet());
    }

    private void writeXconnect(InstanceIdentifier<ConnectionPoints> id,
                               ConnectionPoints dataAfter,
                               Endpoint endpoint1,
                               Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        Endpoint local = getLocal(endpoint1, endpoint2);
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(local);
        Endpoint remote = getRemote(endpoint1, endpoint2);

        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("pseudowire-class %s", netName),
                "encapsulation mpls",
                "exit",
                f("interface %s", ifc1),
                f("xconnect %s %s pw-class %s",
                        remote.getRemote().getConfig().getRemoteSystem().getIpv4Address().getValue(),
                        remote.getRemote().getConfig().getVirtualCircuitIdentifier(),
                        netName),
                "end");
    }

    private Endpoint getRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == REMOTE.class ? endpoint1 : endpoint2;
    }

    private Endpoint getLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == LOCAL.class ? endpoint1 : endpoint2;
    }

    private void deleteXconnect(InstanceIdentifier<ConnectionPoints> id,
                                Endpoint endpoint1, Endpoint endpoint2) throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(getLocal(endpoint1, endpoint2));
        blockingDeleteAndRead(cli, id,
                "conf t",
                f("no pseudowire-class %s", netName),
                f("interface %s", ifc1),
                "no xconnect",
                "end");
    }

    private void writeLocalConnect(InstanceIdentifier<ConnectionPoints> id,
                                   ConnectionPoints dataAfter,
                                   Endpoint endpoint1,
                                   Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);
        L2P2PConnectionPointsReader.InterfaceId ifc2 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint2);
        String output = blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("connect %s %s %s interworking ethernet", netName, ifc1, ifc2),
                "end");

        checkState(!output.toLowerCase().contains("invalid command"), "Local connect configuration failed with output: %s", output);
    }

    private void deleteLocalConnect(InstanceIdentifier<ConnectionPoints> id) throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(cli, id,
                "conf t",
                f("no connect %s", netName),
                "end");
    }

    public static Endpoint getEndpoint(ConnectionPoint connectionPoint1, WriteContext writeContext, Set<String> usedInterfaces, boolean isWrite, boolean checkSubifc) {
        Endpoint endpoint1 = connectionPoint1.getEndpoints().getEndpoint().get(0);
        checkArgument(endpoint1.getEndpointId().equals(L2P2PConnectionPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s", L2P2PConnectionPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal().getConfig() != null) {

            String ifcName = endpoint1.getLocal().getConfig().getInterface();
            L2P2PConnectionPointsReader.InterfaceId interfaceId = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);

            if (isWrite) {
                checkIfcExists(writeContext, isWrite, interfaceId);
                checkIfcNotUsedAlready(usedInterfaces, interfaceId);
            }

            Object subifc = endpoint1.getLocal().getConfig().getSubinterface();
            if (subifc != null) {
                if (checkSubifc && isWrite) {
                    checkSubIfcExists(writeContext, ifcName, interfaceId, (Long) subifc);
                } else if(isWrite) {
                    checkSubIfcDoesntExist(writeContext, ifcName, interfaceId, (Long) subifc);
                }
            }

            if (isWrite) {
                checkInterfaceNoIp(writeContext, endpoint1.getLocal().getConfig(), interfaceId);
            }
        }

        return endpoint1;
    }

    private static void checkIfcNotUsedAlready(Set<String> usedInterfaces, L2P2PConnectionPointsReader.InterfaceId interfaceId) {
        // Check interface not used already (or its parent)
        checkArgument(!usedInterfaces.contains(interfaceId.toString()),
                "Interface %s already used in L2VPN network as: %s", interfaceId, interfaceId);
        checkArgument(!usedInterfaces.contains(interfaceId.toParentIfcString()),
                "Interface %s already used in L2VPN network as: %s", interfaceId, interfaceId.toParentIfcString());
    }

    private static void checkSubIfcExists(WriteContext writeContext, String ifcName, L2P2PConnectionPointsReader.InterfaceId interfaceId, Long subifc) {
        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifc));

        Optional<Subinterface> subData = writeContext.readAfter(subIfcId);
        checkArgument(subData.isPresent(), "Unknown subinterface %s.%s, cannot configure L2VPN", interfaceId);
    }

    private static void checkSubIfcDoesntExist(WriteContext writeContext, String ifcName, L2P2PConnectionPointsReader.InterfaceId interfaceId, Long subifc) {
        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifc));

        Optional<Subinterface> subData = writeContext.readAfter(subIfcId);
        checkArgument(!subData.isPresent(), "Subinterface %s.%s already exists, cannot configure L2VPN", interfaceId);
    }

    private static void checkIfcExists(WriteContext writeContext, boolean isWrite, L2P2PConnectionPointsReader.InterfaceId interfaceId) {
        KeyedInstanceIdentifier<Interface, InterfaceKey> ifcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(interfaceId.toParentIfcString()));

        Optional<Interface> ifcData = isWrite ? writeContext.readAfter(ifcId) : writeContext.readBefore(ifcId);
        checkArgument(ifcData.isPresent(), "Unknown interface %s, cannot configure L2VPN", ifcData);
    }

    private static void checkInterfaceNoIp(WriteContext writeContext,
                                           Config localConfig,
                                           L2P2PConnectionPointsReader.InterfaceId interfaceId) {
        Long subifcId = localConfig.getSubinterface() == null ? 0 : (Long) localConfig.getSubinterface();

        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(localConfig.getInterface()))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifcId));

        Optional<Addresses> ipv4 = writeContext.readAfter(subIfcId.augmentation(Subinterface1.class)
                .child(Ipv4.class)
                .child(Addresses.class));
        // Ensure no IPv4
        checkState(!ipv4.isPresent() || ipv4.get().getAddress().isEmpty(),
                "Interface: %s cannot be used in L2VPN. It has IP address configured: %s", interfaceId, ipv4);

        Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.Addresses> ipv6 = writeContext.readAfter(
                subIfcId.augmentation(Subinterface2.class)
                        .child(Ipv6.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.Addresses.class));
        // Ensure no IPv6
        checkState(!ipv6.isPresent() || ipv6.get().getAddress().isEmpty(),
                "Interface: %s cannot be used in L2VPN. It has IPv6 address configured: %s", interfaceId, ipv6);
    }

    public static ConnectionPoint getCPoint(@Nonnull ConnectionPoints dataAfter, String expectedPointId) {
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
        ConnectionPoint connectionPoint1 = getCPoint(dataBefore, L2P2PConnectionPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, Collections.emptySet(), false, true);

        ConnectionPoint connectionPoint2 = getCPoint(dataBefore, L2P2PConnectionPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, Collections.emptySet(), false, true);

        if (isLocal(endpoint1, endpoint2)) {
            deleteLocalConnect(id);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            deleteXconnect(id, endpoint1, endpoint2);
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

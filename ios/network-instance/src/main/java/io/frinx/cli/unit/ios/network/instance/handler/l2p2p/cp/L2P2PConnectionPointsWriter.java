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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
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

public class L2P2PConnectionPointsWriter implements CliWriter<ConnectionPoints>,
        CompositeWriter.Child<ConnectionPoints> {

    public static final int NET_NAME_LENGTH = 15;
    private Cli cli;

    public L2P2PConnectionPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                 @NotNull ConnectionPoints dataAfter,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P).canProcess(id, writeContext, false)) {
            return false;
        }

        Preconditions.checkArgument(dataAfter.getConnectionPoint()
                        .size() == 2,
                "L2P2P network only supports 2 endpoints, but were: %s", dataAfter.getConnectionPoint());
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        Preconditions.checkArgument(netName.length() <= NET_NAME_LENGTH,
                "L2P2P network name is too long: %s. Maximum chars is: %s", netName, NET_NAME_LENGTH);

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
            throw new IllegalArgumentException("Unable to configure L2P2P with REMOTE only endpoints: " + dataAfter
                    .getConnectionPoint());
        }
        return true;
    }

    public static Set<String> getUsedInterfaces(@NotNull InstanceIdentifier<ConnectionPoints> id, @NotNull
            WriteContext writeContext) {
        return writeContext.readAfter(io.frinx.openconfig.openconfig.network.instance.IIDs.NETWORKINSTANCES)
                .get()
                .getNetworkInstance()
                .stream()
                .filter(instance -> instance.getConfig()
                        .getType() == L2P2P.class || instance.getConfig()
                        .getType() == L2VSI.class)
                .filter(instance -> !Objects.equals(instance.getName(), id.firstKeyOf(NetworkInstance.class)
                        .getName()))
                .flatMap(instance -> instance.getConnectionPoints()
                        .getConnectionPoint()
                        .stream())
                .flatMap(connPoint -> connPoint.getEndpoints()
                        .getEndpoint()
                        .stream())
                .filter(endpoint -> endpoint.getConfig()
                        .getType() == LOCAL.class)
                .map(L2P2PConnectionPointsReader.InterfaceId::fromEndpoint)
                .map(L2P2PConnectionPointsReader.InterfaceId::toString)
                .collect(Collectors.toSet());
    }

    private void writeXconnect(InstanceIdentifier<ConnectionPoints> id,
                               ConnectionPoints dataAfter,
                               Endpoint endpoint1,
                               Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        Endpoint local = getLocal(endpoint1, endpoint2);
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(local);
        Endpoint remote = getRemote(endpoint1, endpoint2);

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("pseudowire-class %s", netName),
                "encapsulation mpls",
                "exit",
                f("interface %s", ifc1),
                f("xconnect %s %s pw-class %s",
                        remote.getRemote()
                                .getConfig()
                                .getRemoteSystem()
                                .getIpv4Address()
                                .getValue(),
                        remote.getRemote()
                                .getConfig()
                                .getVirtualCircuitIdentifier(),
                        netName),
                "end");
    }

    private Endpoint getRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig()
                .getType() == REMOTE.class ? endpoint1 : endpoint2;
    }

    private Endpoint getLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig()
                .getType() == LOCAL.class ? endpoint1 : endpoint2;
    }

    private void deleteXconnect(InstanceIdentifier<ConnectionPoints> id,
                                Endpoint endpoint1, Endpoint endpoint2) throws WriteFailedException
            .DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId
                .fromEndpoint(getLocal(endpoint1, endpoint2));
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("no pseudowire-class %s", netName),
                f("interface %s", ifc1),
                "no xconnect",
                "end");
    }

    private void writeLocalConnect(InstanceIdentifier<ConnectionPoints> id,
                                   ConnectionPoints dataAfter,
                                   Endpoint endpoint1,
                                   Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);
        L2P2PConnectionPointsReader.InterfaceId ifc2 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint2);
        String output = blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("connect %s %s %s interworking ethernet", netName, ifc1, ifc2),
                "end");

        Preconditions.checkState(!output.toLowerCase(Locale.ROOT)
                .contains("invalid command"), "Local connect configuration failed with output: %s", output);
    }

    private void deleteLocalConnect(InstanceIdentifier<ConnectionPoints> id) throws WriteFailedException
            .DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("no connect %s", netName),
                "end");
    }

    public static Endpoint getEndpoint(ConnectionPoint connectionPoint1, WriteContext writeContext, Set<String>
            usedInterfaces, boolean isWrite, boolean checkSubifc) {
        Endpoint endpoint1 = connectionPoint1.getEndpoints()
                .getEndpoint()
                .get(0);
        Preconditions.checkArgument(endpoint1.getEndpointId()
                        .equals(L2P2PConnectionPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s", L2P2PConnectionPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal()
                .getConfig() != null) {

            String ifcName = endpoint1.getLocal()
                    .getConfig()
                    .getInterface();
            L2P2PConnectionPointsReader.InterfaceId interfaceId = L2P2PConnectionPointsReader.InterfaceId
                    .fromEndpoint(endpoint1);

            if (isWrite) {
                checkIfcExists(writeContext, isWrite, interfaceId);
                checkIfcNotUsedAlready(usedInterfaces, interfaceId);
            }

            Object subifc = endpoint1.getLocal()
                    .getConfig()
                    .getSubinterface();
            if (subifc != null) {
                if (checkSubifc && isWrite) {
                    checkSubIfcExists(writeContext, ifcName, interfaceId, (Long) subifc);
                } else if (isWrite) {
                    checkSubIfcDoesntExist(writeContext, ifcName, interfaceId, (Long) subifc);
                }
            }

            if (isWrite) {
                checkInterfaceNoIp(writeContext, endpoint1.getLocal()
                        .getConfig(), interfaceId);
            }
        }

        return endpoint1;
    }

    private static void checkIfcNotUsedAlready(Set<String> usedInterfaces, L2P2PConnectionPointsReader.InterfaceId
            interfaceId) {
        // Check interface not used already (or its parent)
        Preconditions.checkArgument(!usedInterfaces.contains(interfaceId.toString()),
                "Interface %s already used in L2VPN network as: %s", interfaceId, interfaceId);
        Preconditions.checkArgument(!usedInterfaces.contains(interfaceId.toParentIfcString()),
                "Interface %s already used in L2VPN network as: %s", interfaceId, interfaceId.toParentIfcString());
    }

    private static void checkSubIfcExists(WriteContext writeContext, String ifcName, L2P2PConnectionPointsReader
            .InterfaceId interfaceId, Long subifc) {
        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifc));

        Optional<Subinterface> subData = writeContext.readAfter(subIfcId);
        Preconditions.checkArgument(subData.isPresent(),
                "Unknown subinterface %s, cannot configure L2VPN", interfaceId);
    }

    private static void checkSubIfcDoesntExist(WriteContext writeContext, String ifcName, L2P2PConnectionPointsReader
            .InterfaceId interfaceId, Long subifc) {
        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(ifcName))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifc));

        Optional<Subinterface> subData = writeContext.readAfter(subIfcId);
        Preconditions.checkArgument(!subData.isPresent(),
                "Subinterface %s already exists, cannot configure L2VPN", interfaceId);
    }

    private static void checkIfcExists(WriteContext writeContext, boolean isWrite, L2P2PConnectionPointsReader
            .InterfaceId interfaceId) {
        KeyedInstanceIdentifier<Interface, InterfaceKey> ifcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(interfaceId.toParentIfcString()));

        Optional<Interface> ifcData = isWrite ? writeContext.readAfter(ifcId) : writeContext.readBefore(ifcId);
        Preconditions.checkArgument(ifcData.isPresent(), "Unknown interface %s, cannot configure L2VPN", ifcData);
    }

    private static void checkInterfaceNoIp(WriteContext writeContext,
                                           Config localConfig,
                                           L2P2PConnectionPointsReader.InterfaceId interfaceId) {
        Long subifcId = localConfig.getSubinterface() == null ? Long.valueOf(0) : (Long) localConfig.getSubinterface();

        KeyedInstanceIdentifier<Subinterface, SubinterfaceKey> subIfcId = IIDs.INTERFACES
                .child(Interface.class, new InterfaceKey(localConfig.getInterface()))
                .child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subifcId));

        Optional<Addresses> ipv4 = writeContext.readAfter(subIfcId.augmentation(Subinterface1.class)
                .child(Ipv4.class)
                .child(Addresses.class));
        // Ensure no IPv4
        Preconditions.checkState(!ipv4.isPresent() || ipv4.get()
                        .getAddress()
                        .isEmpty(),
                "Interface: %s cannot be used in L2VPN. It has IP address configured: %s", interfaceId, ipv4);

        Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top
                .ipv6.Addresses> ipv6 = writeContext.readAfter(
                subIfcId.augmentation(Subinterface2.class)
                        .child(Ipv6.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip
                                .rev161222.ipv6.top.ipv6.Addresses.class));
        // Ensure no IPv6
        Preconditions.checkState(!ipv6.isPresent() || ipv6.get()
                        .getAddress()
                        .isEmpty(),
                "Interface: %s cannot be used in L2VPN. It has IPv6 address configured: %s", interfaceId, ipv6);
    }

    public static ConnectionPoint getCPoint(@NotNull ConnectionPoints dataAfter, String expectedPointId) {
        ConnectionPoint connectionPoint = dataAfter.getConnectionPoint()
                .stream()
                .filter(cp -> cp.getKey()
                        .getConnectionPointId()
                        .equals(expectedPointId))
                .findFirst()
                .get();

        Preconditions.checkArgument(connectionPoint.getConnectionPointId()
                        .equals(expectedPointId),
                "Connection point has to be named: %s, not %s", expectedPointId, connectionPoint.getConfig());
        Preconditions.checkArgument(connectionPoint.getEndpoints()
                        .getEndpoint()
                        .size() == 1,
                "Connection point must contain only 1 endpoint, but has: %s", connectionPoint.getEndpoints()
                        .getEndpoint());
        return connectionPoint;
    }

    private static boolean isLocalRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig()
                .getType() != endpoint2.getConfig()
                .getType();
    }

    private static boolean isLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig()
                .getType() == LOCAL.class
                &&
                endpoint2.getConfig()
                        .getType() == LOCAL.class;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P).canProcess(id, writeContext, true)) {
            return false;
        }

        ConnectionPoint connectionPoint1 = getCPoint(dataBefore, L2P2PConnectionPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, Collections.emptySet(), false, true);

        ConnectionPoint connectionPoint2 = getCPoint(dataBefore, L2P2PConnectionPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, Collections.emptySet(), false, true);

        if (isLocal(endpoint1, endpoint2)) {
            deleteLocalConnect(id);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            deleteXconnect(id, endpoint1, endpoint2);
        }
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull ConnectionPoints dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P).canProcess(id, writeContext, false)) {
            return false;
        }
        // there may be more xconnects (check tests), it is necessary to first delete existing
        deleteCurrentAttributesWResult(id, dataBefore, writeContext);
        writeCurrentAttributesWResult(id, dataAfter, writeContext);
        return true;
    }
}
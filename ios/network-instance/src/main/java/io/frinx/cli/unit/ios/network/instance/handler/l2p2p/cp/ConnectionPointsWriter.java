package io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.network.instance.common.L2p2pWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.L2P2P;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
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

        ConnectionPoint connectionPoint1 = getCPoint(dataAfter, ConnectionPointsReader.POINT_1, 0);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, usedInterfaces);

        ConnectionPoint connectionPoint2 = getCPoint(dataAfter, ConnectionPointsReader.POINT_2, 1);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, usedInterfaces);

        if (isLocal(endpoint1, endpoint2)) {
            writeLocalConnect(id, dataAfter, endpoint1, endpoint2);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            writeXconnect(id, dataAfter, endpoint1, endpoint2);
        } else {
            throw new IllegalArgumentException("Unable to configure L2P2P with REMOTE only endpoints: " + dataAfter.getConnectionPoint());
        }
    }

    private static Set<String> getUsedInterfaces(@Nonnull InstanceIdentifier<ConnectionPoints> id, @Nonnull WriteContext writeContext) {
        return writeContext.readAfter(io.frinx.openconfig.openconfig.network.instance.IIDs.NETWORKINSTANCES).get()
                .getNetworkInstance()
                .stream()
                .filter(instance -> instance.getConfig().getType() == L2P2P.class)
                .filter(instance -> !Objects.equals(instance.getName(), id.firstKeyOf(NetworkInstance.class).getName()))
                .flatMap(instance -> instance.getConnectionPoints().getConnectionPoint().stream())
                .flatMap(connPoint -> connPoint.getEndpoints().getEndpoint().stream())
                .filter(endpoint -> endpoint.getConfig().getType() == LOCAL.class)
                .map(ConnectionPointsReader.InterfaceId::fromEndpoint)
                .map(ConnectionPointsReader.InterfaceId::toString)
                .collect(Collectors.toSet());
    }

    private void writeXconnect(InstanceIdentifier<ConnectionPoints> id,
                               ConnectionPoints dataAfter,
                               Endpoint endpoint1,
                               Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        Endpoint local = getLocal(endpoint1, endpoint2);
        ConnectionPointsReader.InterfaceId ifc1 = ConnectionPointsReader.InterfaceId.fromEndpoint(local);
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
                "exit",
                "exit",
                "exit");
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
        ConnectionPointsReader.InterfaceId ifc1 = ConnectionPointsReader.InterfaceId.fromEndpoint(getLocal(endpoint1, endpoint2));
        blockingDeleteAndRead(cli, id,
                "conf t",
                f("no pseudowire-class %s", netName),
                f("interface %s", ifc1),
                "no xconnect",
                "exit",
                "exit");
    }

    private void writeLocalConnect(InstanceIdentifier<ConnectionPoints> id,
                                   ConnectionPoints dataAfter,
                                   Endpoint endpoint1,
                                   Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        ConnectionPointsReader.InterfaceId ifc1 = ConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);
        ConnectionPointsReader.InterfaceId ifc2 = ConnectionPointsReader.InterfaceId.fromEndpoint(endpoint2);
        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("connect %s %s %s interworking ethernet", netName, ifc1, ifc2),
                "exit",
                "exit");
    }

    private void deleteLocalConnect(InstanceIdentifier<ConnectionPoints> id) throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(cli, id,
                "conf t",
                f("no connect %s", netName),
                "exit");
    }

    private static Endpoint getEndpoint(ConnectionPoint connectionPoint1, WriteContext writeContext, Set<String> usedInterfaces) {
        Endpoint endpoint1 = connectionPoint1.getEndpoints().getEndpoint().get(0);
        checkArgument(endpoint1.getEndpointId().equals(ConnectionPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s", ConnectionPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal().getConfig() != null) {

            String ifcName = endpoint1.getLocal().getConfig().getInterface();
            ConnectionPointsReader.InterfaceId interfaceId = ConnectionPointsReader.InterfaceId.fromEndpoint(endpoint1);

            Optional<Interface> ifcData = writeContext.readAfter(IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName)));
            checkArgument(ifcData.isPresent(), "Unknown interface %s, cannot configure l2p2p", ifcData);

            // Check interface not used already (or its parent)
            checkArgument(!usedInterfaces.contains(interfaceId.toString()),
                    "Interface %s already used in L2P2P network as: %s", interfaceId, interfaceId);
            checkArgument(!usedInterfaces.contains(interfaceId.toParentIfcString()),
                    "Interface %s already used in L2P2P network as: %s", interfaceId, interfaceId.toParentIfcString());

            Object subifc = endpoint1.getLocal().getConfig().getSubinterface();
            if (subifc != null) {
                Optional<Subinterface> subData = writeContext.readAfter(
                        IIDs.INTERFACES.child(Interface.class, new InterfaceKey(ifcName))
                                .child(Subinterfaces.class)
                                .child(Subinterface.class, new SubinterfaceKey(((Long) subifc))));
                checkArgument(subData.isPresent(), "Unknown subinterface %s.%s, cannot configure l2p2p", ifcName, subData);
            }
        }
        return endpoint1;
    }

    private static ConnectionPoint getCPoint(@Nonnull ConnectionPoints dataAfter, String expectedPointId, int index) {
        ConnectionPoint connectionPoint = dataAfter.getConnectionPoint().get(index);
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
        ConnectionPoint connectionPoint1 = getCPoint(dataBefore, ConnectionPointsReader.POINT_1, 0);
        Endpoint endpoint1 = getEndpoint(connectionPoint1, writeContext, Collections.emptySet());

        ConnectionPoint connectionPoint2 = getCPoint(dataBefore, ConnectionPointsReader.POINT_2, 1);
        Endpoint endpoint2 = getEndpoint(connectionPoint2, writeContext, Collections.emptySet());

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

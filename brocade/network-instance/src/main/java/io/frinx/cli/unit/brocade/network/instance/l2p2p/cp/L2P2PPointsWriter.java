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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2P2PPointsWriter implements CompositeWriter.Child<ConnectionPoints>, CliWriter<ConnectionPoints> {

    public static final Check L2P2P_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2P2P);

    static String DELETE_VLL = "configure terminal\n"
            + "router mpls\n"
            + "no vll {$network} {$remote.remote.config.virtual_circuit_identifier}\n"
            + "end\n";

    static String VLL_REMOTE = "configure terminal\n"
            + "router mpls\n"
            + "vll {$network} {$remote.remote.config.virtual_circuit_identifier}\n"
            + "vll-peer {$remote.remote.config.remote_system.ipv4_address.value}\n"
            + "end\n";

    static String VLL_IFC = "configure terminal\n"
            + "router mpls\n"
            + "vll {$network} {$remote.remote.config.virtual_circuit_identifier}\n"
            + "untag {$local.local.config.interface}\n"
            + "end\n";

    static String VLL_SUBINTERFACE = "configure terminal\n"
            + "router mpls\n"
            + "vll {$network} {$remote.remote.config.virtual_circuit_identifier}\n"
            + "vlan {$local.local.config.subinterface}\n"
            + "tag {$local.local.config.interface}\n"
            + "end\n";

    static String VLL_LOCAL_IFC = "configure terminal\n"
            + "router mpls\n"
            + "vll-local {$network}\n"
            + "untag {$local.local.config.interface}\n"
            + "end\n";

    static String DELETE_VLL_LOCAL = "configure terminal\n"
            + "router mpls\n"
            + "no vll-local {$network}\n"
            + "end\n";

    static String VLL_LOCAL_SUBINTERFACE = "configure terminal\n"
            + "router mpls\n"
            + "vll-local {$network}\n"
            + "vlan {$local.local.config.subinterface}\n"
            + "tag {$local.local.config.interface}\n"
            + "end\n";

    private Cli cli;

    public L2P2PPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                                 @Nonnull ConnectionPoints dataAfter,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!L2P2P_CHECK.canProcess(id, writeContext, false)) {
            return false;
        }

        Preconditions.checkArgument(dataAfter.getConnectionPoint().size() == 2,
                "L2P2P network only supports 2 endpoints, but were: %s",
                dataAfter.getConnectionPoint());

        ConnectionPoint connectionPoint1 = getCPoint(dataAfter, L2P2PPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(id, connectionPoint1, writeContext, true);

        ConnectionPoint connectionPoint2 = getCPoint(dataAfter, L2P2PPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(id, connectionPoint2, writeContext, true);

        if (isLocal(endpoint1, endpoint2)) {
            writeVllLocal(id, dataAfter, endpoint1, endpoint2);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            writeVll(id, dataAfter, endpoint1, endpoint2);
        } else {
            throw new IllegalArgumentException("Unable to configure L2P2P with REMOTE only endpoints: "
                    + dataAfter.getConnectionPoint());
        }

        return true;
    }

    private void writeVll(InstanceIdentifier<ConnectionPoints> id,
                          ConnectionPoints dataAfter,
                          Endpoint endpoint1,
                          Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        Endpoint local = getLocal(endpoint1, endpoint2);
        Endpoint remote = getRemote(endpoint1, endpoint2);

        blockingWriteAndRead(cli, id, dataAfter, fT(VLL_REMOTE, "network", netName, "remote", remote));

        String template = local.getLocal().getConfig().getSubinterface() == null ? VLL_IFC : VLL_SUBINTERFACE;
        blockingWriteAndRead(cli, id, dataAfter, fT(template, "network", netName, "remote", remote, "local", local));
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

        blockingDeleteAndRead(cli, id, fT(DELETE_VLL, "network", netName, "remote", remote));
    }

    private void writeVllLocal(InstanceIdentifier<ConnectionPoints> id,
                               ConnectionPoints dataAfter,
                               Endpoint endpoint1,
                               Endpoint endpoint2) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        writeVllLocalEndpoint(id, dataAfter, endpoint1, netName);
        writeVllLocalEndpoint(id, dataAfter, endpoint2, netName);
    }

    private void writeVllLocalEndpoint(InstanceIdentifier<ConnectionPoints> id,
                                       ConnectionPoints dataAfter,
                                       Endpoint endpoint,
                                       String netName) throws WriteFailedException.CreateFailedException {

        String template = endpoint.getLocal().getConfig().getSubinterface() == null
                ? VLL_LOCAL_IFC
                : VLL_LOCAL_SUBINTERFACE;
        blockingWriteAndRead(cli, id, dataAfter, fT(template, "network", netName, "local", endpoint));
    }

    private void deleteVllLocal(InstanceIdentifier<ConnectionPoints> id) throws WriteFailedException
            .DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_VLL_LOCAL, "network", netName));
    }

    private static Endpoint getEndpoint(InstanceIdentifier<ConnectionPoints> id,
                                        ConnectionPoint connectionPoint1,
                                        WriteContext writeContext,
                                        boolean isWrite) {
        Endpoint endpoint1 = connectionPoint1.getEndpoints().getEndpoint().get(0);
        Preconditions.checkArgument(endpoint1.getEndpointId().equals(L2P2PPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s",
                L2P2PPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal().getConfig() != null) {

            L2P2PPointsReader.InterfaceId interfaceId = L2P2PPointsReader.InterfaceId.fromEndpoint(endpoint1);

            getInterfaceData(writeContext, isWrite, interfaceId);

            // No subinterface verification required here

            // Verify that interface used in CP is also listed under network-instance
            checkInterfaceInNetworkInstance(writeContext, isWrite, id, interfaceId);
        }

        return endpoint1;
    }

    private static void checkInterfaceInNetworkInstance(WriteContext writeContext,
                                                        boolean isWrite,
                                                        InstanceIdentifier<ConnectionPoints> id,
                                                        L2P2PPointsReader.InterfaceId interfaceId) {
        NetworkInstanceKey neKey = id.firstKeyOf(NetworkInstance.class);

        InstanceIdentifier<Interfaces> ifcInNeId = IidUtils.createIid(
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_INTERFACES,
                neKey);

        boolean interfaceInNe = (isWrite ? writeContext.readAfter(ifcInNeId) : writeContext.readBefore(ifcInNeId))
                .transform(Interfaces::getInterface)
                .or(Collections.emptyList())
                .stream()
                .anyMatch(ifc -> ifc.getId().equals(interfaceId.toParentIfcString()));

        Preconditions.checkArgument(interfaceInNe, "Interface %s is not part of %s", interfaceId, neKey.getName());
    }

    private static Interface getInterfaceData(WriteContext writeContext,
                                              boolean isWrite,
                                              L2P2PPointsReader.InterfaceId ifcName) {
        InterfaceKey key = new InterfaceKey(ifcName.toParentIfcString());
        InstanceIdentifier<Interface> id = IidUtils.createIid(IIDs.IN_INTERFACE, key);
        Optional<Interface> ifcData = isWrite ? writeContext.readAfter(id) : writeContext.readBefore(id);

        Preconditions.checkArgument(ifcData.isPresent(),
                "Unknown interface %s, cannot configure l2p2p", ifcName);

        return ifcData.get();
    }

    private static ConnectionPoint getCPoint(ConnectionPoints dataAfter, String expectedPointId) {
        ConnectionPoint connectionPoint = dataAfter.getConnectionPoint().stream().filter(cp -> cp.getKey()
                .getConnectionPointId().equals(expectedPointId)).findFirst().get();

        Preconditions.checkArgument(connectionPoint.getConnectionPointId().equals(expectedPointId),
                "Connection point has to be named: %s, not %s",
                expectedPointId, connectionPoint.getConfig());
        Preconditions.checkArgument(connectionPoint.getEndpoints().getEndpoint().size() == 1,
                "Connection point must contain only 1 endpoint, but has: %s",
                connectionPoint.getEndpoints().getEndpoint());
        return connectionPoint;
    }

    private static boolean isLocalRemote(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() != endpoint2.getConfig().getType();
    }

    private static boolean isLocal(Endpoint endpoint1, Endpoint endpoint2) {
        return endpoint1.getConfig().getType() == LOCAL.class
                && endpoint2.getConfig().getType() == LOCAL.class;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2P2P_CHECK.canProcess(id, writeContext, true)) {
            return false;
        }

        ConnectionPoint connectionPoint1 = getCPoint(dataBefore, L2P2PPointsReader.POINT_1);
        Endpoint endpoint1 = getEndpoint(id, connectionPoint1, writeContext, false);

        ConnectionPoint connectionPoint2 = getCPoint(dataBefore, L2P2PPointsReader.POINT_2);
        Endpoint endpoint2 = getEndpoint(id, connectionPoint2, writeContext, false);

        if (isLocal(endpoint1, endpoint2)) {
            deleteVllLocal(id);
        } else if (isLocalRemote(endpoint1, endpoint2)) {
            deleteVll(id, endpoint1, endpoint2);
        }

        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                                  @Nonnull ConnectionPoints dataBefore,
                                                  @Nonnull ConnectionPoints dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }
}

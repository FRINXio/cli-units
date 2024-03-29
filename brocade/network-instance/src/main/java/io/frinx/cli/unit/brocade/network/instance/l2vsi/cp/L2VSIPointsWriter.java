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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.cp.L2P2PPointsReader;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.ifc.L2VSIInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.BrocadeCpExtensionLocal;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.NiCpBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIPointsWriter implements CompositeWriter.Child<ConnectionPoints>, CliWriter<ConnectionPoints> {

    static String DELETE_VPLS = """
            configure terminal
            router mpls
            no vpls {$network} {$remote.remote.config.virtual_circuit_identifier}
            end
            """;

    @SuppressWarnings("checkstyle:linelength")
    static String VPLS_REMOTE = """
            configure terminal
            router mpls
            vpls {$network} {$vccid}
            {% if ($mtu) %}vpls-mtu {$mtu}
            {% endif %}{% loop in $remotes as $remote %}vpls-peer {$remote.remote.config.remote_system.ipv4_address.value}
            {% endloop %}end
            """;

    static String VPLS_IFC_UNTAG = """
            configure terminal
            router mpls
            vpls {$network} {$vccid}
            vlan {$local.local.config.subinterface}
            untag {$local.local.config.interface}
            end
            """;

    static String VPLS_IFC_TAG = """
            configure terminal
            router mpls
            vpls {$network} {$vccid}
            vlan {$local.local.config.subinterface}
            tag {$local.local.config.interface}
            end
            """;

    private Cli cli;

    public L2VSIPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                 @NotNull ConnectionPoints dataAfter,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(id, writeContext, false)) {
            return false;
        }

        Preconditions.checkArgument(dataAfter.getConnectionPoint().size() >= 2,
                "L2VSI network supports 2 or more endpoints, but were: %s",
                dataAfter.getConnectionPoint());

        List<Endpoint> endpoints = getEndpoints(id, dataAfter, writeContext, true);
        List<Endpoint> locals = endpoints.stream().filter(ep -> ep.getLocal() != null).collect(Collectors.toList());
        List<Endpoint> remotes = endpoints.stream().filter(ep -> ep.getRemote() != null).collect(Collectors.toList());

        Preconditions.checkArgument(!locals.isEmpty() && !remotes.isEmpty(),
                "L2VSI network supports at least 1 local and 1 remote endpoint, but were: LOCAL: %s, REMOTE: %s",
                locals.size(), remotes.size());

        Integer mtu = writeContext.readAfter(id.firstIdentifierOf(NetworkInstance.class)).get().getConfig().getMtu();

        writeVpls(id, dataAfter, locals, remotes, mtu);

        return true;
    }

    private void writeVpls(InstanceIdentifier<ConnectionPoints> id,
                           ConnectionPoints dataAfter,
                           List<Endpoint> local,
                           List<Endpoint> remotes, Integer mtu) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        Long vccid = remotes.get(0).getRemote().getConfig().getVirtualCircuitIdentifier();
        long count = remotes.stream()
                .filter(r -> vccid.equals(r.getRemote().getConfig().getVirtualCircuitIdentifier()))
                .count();
        Preconditions.checkArgument(remotes.size() == count, "All remote must have the same VCCID.");

        blockingWriteAndRead(cli, id, dataAfter, fT(VPLS_REMOTE,
                "network", netName, "vccid", vccid, "remotes", remotes, "mtu", mtu));

        for (Endpoint ep : local) {
            boolean isUntagged = java.util.Optional.ofNullable(
                    ep.getLocal().getConfig().getAugmentation(NiCpBrocadeAug.class))
                    .map(BrocadeCpExtensionLocal::isSubinterfaceUntagged)
                    .orElse(false);

            String template = isUntagged ? VPLS_IFC_UNTAG : VPLS_IFC_TAG;
            blockingWriteAndRead(cli, id, dataAfter, fT(template, "network", netName, "vccid", vccid, "local", ep));
        }
    }

    private void deleteVpls(InstanceIdentifier<ConnectionPoints> id, List<Endpoint> locals, List<Endpoint> remotes)
            throws WriteFailedException.DeleteFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        blockingDeleteAndRead(cli, id, fT(DELETE_VPLS, "network", netName, "remote", remotes.get(0)));
    }

    private static Endpoint getEndpoint(InstanceIdentifier<ConnectionPoints> id,
                                        Endpoint endpoint1,
                                        WriteContext writeContext,
                                        boolean isWrite) {
        Preconditions.checkArgument(endpoint1.getEndpointId().equals(L2P2PPointsReader.ENDPOINT_ID),
                "Endpoint has to be named: %s, not %s",
                L2P2PPointsReader.ENDPOINT_ID, endpoint1.getConfig());

        // Verify existing interfaces
        if (endpoint1.getLocal() != null && endpoint1.getLocal().getConfig() != null) {

            L2VSIPointsReader.InterfaceId interfaceId = L2VSIPointsReader.InterfaceId.fromEndpoint(endpoint1);

            getInterfaceData(writeContext, isWrite, interfaceId);
            Preconditions.checkArgument(endpoint1.getLocal().getConfig().getSubinterface() != null,
                    "Subinterface(VLAN) not defined for endpoint: %s", endpoint1);
            // No subinterface verification required here

            // Verify that interface used in CP is also listed under network-instance
            checkInterfaceInNetworkInstance(writeContext, isWrite, id, interfaceId);
        }

        return endpoint1;
    }

    private static void checkInterfaceInNetworkInstance(WriteContext writeContext,
                                                        boolean isWrite,
                                                        InstanceIdentifier<ConnectionPoints> id,
                                                        L2VSIPointsReader.InterfaceId interfaceId) {
        NetworkInstanceKey neKey = id.firstKeyOf(NetworkInstance.class);

        InstanceIdentifier<Interfaces> ifcInNeId = IidUtils.createIid(
                io.frinx.openconfig.openconfig.network.instance.IIDs.NE_NE_INTERFACES,
                neKey);

        boolean interfaceInNe = (isWrite ? writeContext.readAfter(ifcInNeId) : writeContext.readBefore(ifcInNeId))
                .map(Interfaces::getInterface)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(ifc -> ifc.getId().equals(interfaceId.toParentIfcString()));

        Preconditions.checkArgument(interfaceInNe, "Interface %s is not part of %s", interfaceId, neKey.getName());
    }

    private static Interface getInterfaceData(WriteContext writeContext,
                                              boolean isWrite,
                                              L2VSIPointsReader.InterfaceId ifcName) {
        InterfaceKey key = new InterfaceKey(ifcName.toParentIfcString());
        InstanceIdentifier<Interface> id = IidUtils.createIid(IIDs.IN_INTERFACE, key);
        Optional<Interface> ifcData = isWrite ? writeContext.readAfter(id) : writeContext.readBefore(id);

        Preconditions.checkArgument(ifcData.isPresent(), "Unknown interface %s, cannot configure l2vsi", ifcName);

        return ifcData.get();
    }

    private List<Endpoint> getEndpoints(InstanceIdentifier<ConnectionPoints> id,
                                        ConnectionPoints connectionPoints,
                                        WriteContext writeContext,
                                        boolean isWrite) {
        return connectionPoints.getConnectionPoint().stream()
                .flatMap(cp -> Objects.requireNonNull(cp.getEndpoints().getEndpoint()).stream())
                .map(ep -> getEndpoint(id, ep, writeContext, isWrite))
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(id, writeContext, true)) {
            return false;
        }

        List<Endpoint> endpoints = getEndpoints(id, dataBefore, writeContext, false);

        List<Endpoint> locals = endpoints.stream().filter(ep -> ep.getLocal() != null).collect(Collectors.toList());
        List<Endpoint> remotes = endpoints.stream().filter(ep -> ep.getRemote() != null).collect(Collectors.toList());

        if (remotes.isEmpty()) {
            return true;
        }

        deleteVpls(id, locals, remotes);

        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull ConnectionPoints dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }
}
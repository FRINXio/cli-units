/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.network.instance.handler.l2vsi.cp;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsReader;
import io.frinx.cli.unit.iosxe.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConnectionPointsWriter implements CliWriter<ConnectionPoints>,
        CompositeWriter.Child<ConnectionPoints> {

    public static final String SH_RUN_INCLUDE_BRIDGE_DOMAIN = "show running-config | include bridge-domain";
    private Cli cli;
    public static final Pattern BD_PATTERN = Pattern.compile("\\s*bridge-domain\\s+(?<bdIndex>[0-9]+)\\s*");

    public L2VSIConnectionPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                 @NotNull ConnectionPoints dataAfter,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2VSI).canProcess(id, writeContext, false)) {
            return false;
        }

        Preconditions.checkArgument(dataAfter.getConnectionPoint()
                        .size() >= 2,
                "L2VSI network only supports at least 2 endpoints, but were: %s", dataAfter.getConnectionPoint());

        Set<String> usedInterfaces = L2P2PConnectionPointsWriter.getUsedInterfaces(id, writeContext);

        ConnectionPoint remotePoint = L2P2PConnectionPointsWriter.getCPoint(dataAfter,
                L2VSIConnectionPointsReader.REMOTE_POINT_ID);
        Endpoint remoteEndpoint = L2P2PConnectionPointsWriter.getEndpoint(remotePoint, writeContext, usedInterfaces,
                true, false);
        Preconditions.checkArgument(remoteEndpoint.getConfig()
                        .getType() == REMOTE.class,
                "Endpoint %s is not remote is not of type %s", L2VSIConnectionPointsReader.REMOTE_POINT_ID, REMOTE
                        .class);

        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();
        int bdIndex = findNextFreeBd(id, netName, dataAfter);

        writeAutodiscovery(remoteEndpoint, id, dataAfter, writeContext, bdIndex);

        Map<String, Endpoint> locals = dataAfter.getConnectionPoint()
                .stream()
                // skip remote
                .filter(cp -> !cp.getConnectionPointId()
                        .equals(L2VSIConnectionPointsReader.REMOTE_POINT_ID))
                .map(cp -> new AbstractMap.SimpleEntry<>(cp.getConnectionPointId(),
                        L2P2PConnectionPointsWriter.getEndpoint(cp, writeContext, usedInterfaces, true, false)))
                .map(this::ensureLocal)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, Endpoint> local : locals.entrySet()) {
            writeLocal(local.getKey(), local.getValue(), id, dataAfter, bdIndex);
        }
        return true;
    }

    public boolean isValidAsBd(String netName) {
        return NumberUtils.isDigits(netName) && Integer.parseInt(netName) > 0 && Integer.parseInt(netName) < 4096;
    }

    private void writeAutodiscovery(Endpoint remoteEndpoint, InstanceIdentifier<ConnectionPoints> id,
                                    ConnectionPoints dataAfter, WriteContext writeContext, int bdIndex) throws
            WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();

        // FIXME check bridge domain not in use
        // FIXME check vfi not in use

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("l2 vfi %s autodiscovery", netName),
                f("vpn id %s", remoteEndpoint.getRemote()
                        .getConfig()
                        .getVirtualCircuitIdentifier()),
                f("bridge-domain %s", bdIndex),
                "end");
    }

    private int findNextFreeBd(InstanceIdentifier<ConnectionPoints> id, String netName, ConnectionPoints dataAfter)
            throws WriteFailedException.CreateFailedException {
        if (isValidAsBd(netName)) {
            return Integer.parseInt(netName);
        }

        String output = blockingWriteAndRead(SH_RUN_INCLUDE_BRIDGE_DOMAIN, this.cli, id, dataAfter);
        Set<Integer> bdomains = Sets.newHashSet(ParsingUtils.parseFields(output, 0, BD_PATTERN::matcher,
            m -> m.group("bdIndex"),
                Integer::valueOf));
        int bdIndex = -1;

        for (int i = 1; i < 4096; i++) {
            if (!bdomains.contains(i)) {
                bdIndex = i;
                break;
            }
        }

        Preconditions.checkArgument(bdIndex > 0, "Unable to find available bridge domain for L2VPN: %s", id);
        return bdIndex;
    }

    private void deleteAutodiscovery(Endpoint remoteEndpoint, InstanceIdentifier<ConnectionPoints> id,
                                     ConnectionPoints dataBefore) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class)
                .getName();

        blockingWriteAndRead(cli, id, dataBefore,
                "configure terminal",
                f("no l2 vfi %s autodiscovery", netName),
                "end");
    }

    private void writeLocal(String cpId, Endpoint endpoint, InstanceIdentifier<ConnectionPoints> id, ConnectionPoints
            dataAfter, int bdIndex) throws WriteFailedException.CreateFailedException {
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint);

        if (endpoint.getLocal()
                .getConfig()
                .getSubinterface() == null) {
            // TODO check service instance not in use
            blockingWriteAndRead(cli, id, dataAfter,
                    "configure terminal",
                    f("interface %s", ifc1.toParentIfcString()),
                    f("service instance %s ethernet", cpId),
                    "encapsulation untagged",
                    f("bridge-domain %s", bdIndex),
                    "end");
        } else {
            // TODO check service instance not in use
            blockingWriteAndRead(cli, id, dataAfter,
                    "configure terminal",
                    f("interface %s", ifc1.toParentIfcString()),
                    f("service instance %s ethernet", cpId),
                    f("encapsulation dot1q %s", endpoint.getLocal()
                            .getConfig()
                            .getSubinterface()),
                    "rewrite ingress tag pop 1 symmetric",
                    f("bridge-domain %s", bdIndex),
                    "end");
        }
    }

    private void deleteLocal(String cpId, Endpoint endpoint, InstanceIdentifier<ConnectionPoints> id,
                             ConnectionPoints dataBefore) throws WriteFailedException.CreateFailedException {

        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint);

        blockingWriteAndRead(cli, id, dataBefore,
                "configure terminal",
                f("interface %s", ifc1.toParentIfcString()),
                f("no service instance %s ethernet", cpId),
                "end");
    }

    private Map.Entry<String, Endpoint> ensureLocal(AbstractMap.SimpleEntry<String, Endpoint> endpoint) {
        Preconditions.checkArgument(endpoint.getValue()
                        .getConfig()
                        .getType() == LOCAL.class,
                "Endpoint %s is not of type %s, only 1 remote (autodiscovery) is allowed", endpoint.getKey(), LOCAL
                        .class);
        return endpoint;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2VSI).canProcess(id, writeContext, true)) {
            return false;
        }

        ConnectionPoint remotePoint = L2P2PConnectionPointsWriter.getCPoint(dataBefore,
                L2VSIConnectionPointsReader.REMOTE_POINT_ID);
        Endpoint remoteEndpoint = L2P2PConnectionPointsWriter.getEndpoint(remotePoint, writeContext,
                Collections.emptySet(), false, false);

        Map<String, Endpoint> locals = dataBefore.getConnectionPoint()
                .stream()
                // skip remote
                .filter(cp -> !cp.getConnectionPointId()
                        .equals(L2VSIConnectionPointsReader.REMOTE_POINT_ID))
                .map(cp -> new AbstractMap.SimpleEntry<>(cp.getConnectionPointId(),
                        L2P2PConnectionPointsWriter.getEndpoint(cp, writeContext,
                        Collections.emptySet(), false, false)))
                .map(this::ensureLocal)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, Endpoint> local : locals.entrySet()) {
            deleteLocal(local.getKey(), local.getValue(), id, dataBefore);
        }

        deleteAutodiscovery(remoteEndpoint, id, dataBefore);
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<ConnectionPoints> id,
                                                  @NotNull ConnectionPoints dataBefore,
                                                  @NotNull ConnectionPoints dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!BasicCheck.checkData(ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_L2VSI).canProcess(id, writeContext, false)) {
            return false;
        }
        // this is fine, for each cpid there is new command
        deleteCurrentAttributesWResult(id, dataBefore, writeContext);
        writeCurrentAttributesWResult(id, dataAfter, writeContext);
        return true;
    }
}
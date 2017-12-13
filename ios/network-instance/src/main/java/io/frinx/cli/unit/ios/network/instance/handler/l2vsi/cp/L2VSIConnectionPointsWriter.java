/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter.getCPoint;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter.getEndpoint;
import static io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsWriter.getUsedInterfaces;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L2vsiWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.math.NumberUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConnectionPointsWriter implements L2vsiWriter<ConnectionPoints> {

    public static final String SH_RUN_INCLUDE_BRIDGE_DOMAIN = "sh run | include bridge-domain";
    private Cli cli;
    public static final Pattern BD_PATTERN = Pattern.compile("\\s*bridge-domain\\s+(?<bdIndex>[0-9]+)\\s*");

    public L2VSIConnectionPointsWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                              @Nonnull ConnectionPoints dataAfter,
                                              @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkArgument(dataAfter.getConnectionPoint().size() >= 2,
                "L2VSI network only supports at least 2 endpoints, but were: %s", dataAfter.getConnectionPoint());

        Set<String> usedInterfaces = getUsedInterfaces(id, writeContext);

        ConnectionPoint remotePoint = getCPoint(dataAfter, L2VSIConnectionPointsReader.REMOTE_POINT_ID);
        Endpoint remoteEndpoint = getEndpoint(remotePoint, writeContext, usedInterfaces, true, false);
        checkArgument(remoteEndpoint.getConfig().getType() == REMOTE.class,
                "Endpoint %s is not remote is not of type %s", L2VSIConnectionPointsReader.REMOTE_POINT_ID, REMOTE.class);

        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        int bdIndex = findNextFreeBd(id, netName, dataAfter);

        writeAutodiscovery(remoteEndpoint, id, dataAfter, writeContext, bdIndex);

        Map<String, Endpoint> locals = dataAfter.getConnectionPoint().stream()
                // skip remote
                .filter(cp -> !cp.getConnectionPointId().equals(L2VSIConnectionPointsReader.REMOTE_POINT_ID))
                .map(cp -> new AbstractMap.SimpleEntry<>(cp.getConnectionPointId(), getEndpoint(cp, writeContext, usedInterfaces, true, false)))
                .map(this::ensureLocal)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, Endpoint> local : locals.entrySet()) {
            writeLocal(local.getKey(), local.getValue(), id, dataAfter, bdIndex);
        }
    }

    public boolean isValidAsBd(String netName) {
        return NumberUtils.isDigits(netName) && Integer.parseInt(netName) > 0 && Integer.parseInt(netName) < 4096;
    }

    private void writeAutodiscovery(Endpoint remoteEndpoint, InstanceIdentifier<ConnectionPoints> id, ConnectionPoints dataAfter, WriteContext writeContext, int bdIndex) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        // FIXME check bridge domain not in use
        // FIXME check vfi not in use

        blockingWriteAndRead(cli, id, dataAfter,
                "conf t",
                f("l2 vfi %s autodiscovery", netName),
                f("vpn id %s", remoteEndpoint.getRemote().getConfig().getVirtualCircuitIdentifier()),
                f("bridge-domain %s", bdIndex),
                "end");
    }

    private int findNextFreeBd(InstanceIdentifier<ConnectionPoints> id, String netName, ConnectionPoints dataAfter) throws WriteFailedException.CreateFailedException {
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

        checkArgument(bdIndex > 0, "Unable to find available bridge domain for L2VPN: %s", id);
        return bdIndex;
    }

    private void deleteAutodiscovery(Endpoint remoteEndpoint, InstanceIdentifier<ConnectionPoints> id, ConnectionPoints dataBefore) throws WriteFailedException.CreateFailedException {
        String netName = id.firstKeyOf(NetworkInstance.class).getName();

        blockingWriteAndRead(cli, id, dataBefore,
                "conf t",
                f("no l2 vfi %s autodiscovery", netName),
                "end");
    }

    private void writeLocal(String cpId, Endpoint endpoint, InstanceIdentifier<ConnectionPoints> id, ConnectionPoints dataAfter, int bdIndex) throws WriteFailedException.CreateFailedException {
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint);

        if (endpoint.getLocal().getConfig().getSubinterface() == null) {
            // TODO check service instance not in use
            blockingWriteAndRead(cli, id, dataAfter,
                            "conf t",
                            f("interface %s", ifc1),
                            f("service instance %s ethernet", cpId),
                            "encapsulation untagged",
                            f("bridge-domain %s", bdIndex),
                            "end");
        } else {
            // TODO check service instance not in use
            blockingWriteAndRead(cli, id, dataAfter,
                            "conf t",
                            f("interface %s", ifc1.toParentIfcString()),
                            f("service instance %s ethernet", cpId),
                            f("encapsulation dot1q %s", endpoint.getLocal().getConfig().getSubinterface()),
                            "rewrite ingress tag pop 1 symmetric",
                            f("bridge-domain %s", bdIndex),
                            "end");
        }
    }

    private void deleteLocal(String cpId, Endpoint endpoint, InstanceIdentifier<ConnectionPoints> id, ConnectionPoints dataBefore) throws WriteFailedException.CreateFailedException {

        String netName = id.firstKeyOf(NetworkInstance.class).getName();
        L2P2PConnectionPointsReader.InterfaceId ifc1 = L2P2PConnectionPointsReader.InterfaceId.fromEndpoint(endpoint);

        blockingWriteAndRead(cli, id, dataBefore,
                        "conf t",
                        f("interface %s", ifc1),
                        f("no service instance %s ethernet", cpId),
                        "end");
    }

    private Map.Entry<String, Endpoint> ensureLocal(AbstractMap.SimpleEntry<String, Endpoint> endpoint) {
        checkArgument(endpoint.getValue().getConfig().getType() == LOCAL.class,
                "Endpoint %s is not of type %s, only 1 remote (autodiscovery) is allowed", endpoint.getKey(), LOCAL.class);
        return endpoint;
    }

    @Override
    public void deleteCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                               @Nonnull ConnectionPoints dataBefore,
                                               @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        ConnectionPoint remotePoint = getCPoint(dataBefore, L2VSIConnectionPointsReader.REMOTE_POINT_ID);
        Endpoint remoteEndpoint = getEndpoint(remotePoint, writeContext, Collections.emptySet(), false, false);

        Map<String, Endpoint> locals = dataBefore.getConnectionPoint().stream()
                // skip remote
                .filter(cp -> !cp.getConnectionPointId().equals(L2VSIConnectionPointsReader.REMOTE_POINT_ID))
                .map(cp -> new AbstractMap.SimpleEntry<>(cp.getConnectionPointId(), getEndpoint(cp, writeContext, Collections.emptySet(), true, false)))
                .map(this::ensureLocal)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        for (Map.Entry<String, Endpoint> local : locals.entrySet()) {
            deleteLocal(local.getKey(), local.getValue(), id, dataBefore);
        }

        deleteAutodiscovery(remoteEndpoint, id, dataBefore);
    }

    @Override
    public void updateCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                               @Nonnull ConnectionPoints dataBefore,
                                               @Nonnull ConnectionPoints dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }
}

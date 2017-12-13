/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp;

import static io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIReader.L2_VFI_LINE;
import static io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIReader.realignL2vsi;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.read.Reader;
import io.frinx.cli.handlers.network.instance.L2vsiReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2p2p.cp.L2P2PConnectionPointsReader;
import io.frinx.cli.unit.ios.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPointsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConnectionPointsReader implements L2vsiReader.L2vsiConfigReader<ConnectionPoints, ConnectionPointsBuilder>,
        CompositeReader.Child<ConnectionPoints, ConnectionPointsBuilder> {

    static final String REMOTE_POINT_ID = "remote";

    private static final String SH_L2_VFI_IFC = "sh run | include ^interface|^ service instance|^  bridge-domain";
    private static final Pattern L2_VFI_IFC_LINE = Pattern.compile("interface (?<interface>\\S+)\\s+service instance (?<sId>\\S+) ethernet\\s+bridge-domain (?<bd>\\S+).*");

    private final Cli cli;

    public L2VSIConnectionPointsReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<ConnectionPoints> id,
                                             @Nonnull ConnectionPointsBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        boolean isOper = isOper(ctx);

        String vsiName = id.firstKeyOf(NetworkInstance.class).getName();
        builder.setConnectionPoint(getConnectionPoints(vsiName, id, ctx, isOper));
    }

    private boolean isOper(ReadContext ctx) {
        Object flag = ctx.getModificationCache().get(Reader.DS_TYPE_FLAG);
        return flag != null && flag == LogicalDatastoreType.OPERATIONAL;
    }

    private List<ConnectionPoint> getConnectionPoints(String vfiName, InstanceIdentifier<ConnectionPoints> id, ReadContext ctx, boolean isOper)
            throws ReadFailedException {

        ArrayList<ConnectionPoint> connectionPoints = new ArrayList<>();

        String output = blockingRead(L2VSIReader.SH_L2_VFI, this.cli, id, ctx);
        parseRemotePoint(output, vfiName, isOper).ifPresent(connectionPoints::add);

        Optional<String> bd = parseBridgeDomain(output, vfiName);
        if (bd.isPresent()) {
            output = blockingRead(SH_L2_VFI_IFC, this.cli, id, ctx);
            connectionPoints.addAll(parseLocalPoints(output, vfiName, isOper, bd.get()));
        }

        return connectionPoints;
    }

    private List<ConnectionPoint> parseLocalPoints(String output, String vfiName, boolean isOper, String bd) {
        String linePerL2VsiIfc = realignL2vsiIfcs(output);

        return ParsingUtils.NEWLINE.splitAsStream(linePerL2VsiIfc)
                .map(String::trim)
                .map(L2_VFI_IFC_LINE::matcher)
                .filter(Matcher::matches)
                // bridge domains must match, that tells us interface is part of L2VPNl
                .filter(m -> m.group("bd").equals(bd))
                .map(m -> L2P2PConnectionPointsReader.getConnectionPointBuilder(isOper,
                        L2P2PConnectionPointsReader.getEndpoint(isOper, LOCAL.class)
                                .setLocal(L2P2PConnectionPointsReader.getLocal(isOper, L2P2PConnectionPointsReader.InterfaceId.parse(m.group("interface"))))
                                .build(),
                        m.group("sId"))
                        .build())
                .collect(Collectors.toList());
    }

    private static String realignL2vsiIfcs(String output) {
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface", "\ninterface");
    }

    private Optional<ConnectionPoint> parseRemotePoint(String output, String vfiName, boolean isOper) {
        String linePerL2Vsi = realignL2vsi(output);

        return ParsingUtils.NEWLINE.splitAsStream(linePerL2Vsi)
                .map(String::trim)
                .map(L2_VFI_LINE::matcher)
                .filter(Matcher::matches)
                .filter(m -> m.group("vfi").equals(vfiName))
                .map(m -> m.group("vccid"))
                .findFirst()
                .map(vccId -> L2P2PConnectionPointsReader.getConnectionPointBuilder(isOper,
                        L2P2PConnectionPointsReader.getEndpoint(isOper, REMOTE.class)
                                .setRemote(L2P2PConnectionPointsReader.getRemote(isOper, null, Long.valueOf(vccId)))
                                .build(),
                        REMOTE_POINT_ID)
                        .build());
    }

    private Optional<String> parseBridgeDomain(String output, String vfiName) {
        String linePerL2Vsi = realignL2vsi(output);

        return ParsingUtils.NEWLINE.splitAsStream(linePerL2Vsi)
                .map(String::trim)
                .map(L2_VFI_LINE::matcher)
                .filter(Matcher::matches)
                .filter(m -> m.group("vfi").equals(vfiName))
                .map(m -> m.group("bd"))
                .findFirst();
    }

}

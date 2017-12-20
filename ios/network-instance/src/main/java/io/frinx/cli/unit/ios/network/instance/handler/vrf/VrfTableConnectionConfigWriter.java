/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfTableConnectionConfigWriter implements L3VrfWriter<Config> {

    private final Cli cli;

    public VrfTableConnectionConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        // TODO check if address family is ipv4

        String vrf = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        Long bgpAs = getBgpAs(writeContext, instanceIdentifier);
        String ospfId = getOspfId(writeContext, instanceIdentifier);

        if (BGP.class.equals(config.getSrcProtocol()) && OSPF.class.equals(config.getDstProtocol())) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    "conf t",
                    f("router bpg %s", bgpAs),
                    f("address-family ipv4 %s", vrf, DEFAULT_NETWORK_NAME.equals(vrf) ? "" : f("vrf %s", vrf)),
                    f("redistribute ospf %s", ospfId),
                    "end");
        } else if (BGP.class.equals(config.getDstProtocol()) && OSPF.class.equals(config.getSrcProtocol())) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    "conf t",
                    f("router ospf %s %s", ospfId, DEFAULT_NETWORK_NAME.equals(vrf) ? "" : f("vrf %s", vrf)),
                    f("redistribute bgp %s",bgpAs),
                    "end");
        } else {
            throw new IllegalArgumentException(
                    "Only BGP and OSPF combinations of destination and source protocols are supported");
        }
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    private static Long getBgpAs(WriteContext writeContext, InstanceIdentifier<Config> id) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config> bgpConfigId =
                id.firstIdentifierOf(NetworkInstance.class)
                        .child(Protocols.class)
                        .child(Protocol.class, new ProtocolKey(BGP.class, null))
                        .child(Bgp.class)
                        .child(Global.class)
                        .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config.class);

        String vrf = id.firstKeyOf(NetworkInstance.class).getName();

        Optional<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config> bgpConfigOptional =
                writeContext.readAfter(bgpConfigId);
        Preconditions.checkArgument(bgpConfigOptional.isPresent(), "BGP configurations is not present id vrf %s", vrf);

        return bgpConfigOptional.get().getAs().getValue();
    }

    private static String getOspfId(WriteContext writeContext, InstanceIdentifier<Config> id) {

        InstanceIdentifier<Protocols> protocolsId =
                id.firstIdentifierOf(NetworkInstance.class)
                        .child(Protocols.class);

        String vrf = id.firstKeyOf(NetworkInstance.class).getName();

        Optional<Protocols> vrfProtocols = writeContext.readAfter(protocolsId);
        Preconditions.checkArgument(vrfProtocols.isPresent() && vrfProtocols.get().getProtocol() != null,
                "No protocols specified for vrf %s", vrf);

        return vrfProtocols.get().getProtocol().stream()
                .filter(protocol -> Ospfv2.class.equals(protocol.getIdentifier()))
                .map(Protocol::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ospf specified for vrf " + vrf));
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier,
                                               Config config, WriteContext writeContext) throws WriteFailedException {
        String vrf = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        Long bgpAs = getBgpAs(writeContext, instanceIdentifier);
        String ospfId = getOspfId(writeContext, instanceIdentifier);

        if (BGP.class.equals(config.getSrcProtocol()) && OSPF.class.equals(config.getDstProtocol())) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    "conf t",
                    f("router bpg %s", bgpAs),
                    f("address-family ipv4 %s", vrf, DEFAULT_NETWORK_NAME.equals(vrf) ? "" : f("vrf %s", vrf)),
                    f("no redistribute ospf %s", ospfId),
                    "end");
        } else if (BGP.class.equals(config.getDstProtocol()) && OSPF.class.equals(config.getSrcProtocol())) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    "conf t",
                    f("router ospf %s %s", ospfId, DEFAULT_NETWORK_NAME.equals(vrf) ? "" : f("vrf %s", vrf)),
                    f("no redistribute bgp %s",bgpAs),
                    "end");
        }
    }
}

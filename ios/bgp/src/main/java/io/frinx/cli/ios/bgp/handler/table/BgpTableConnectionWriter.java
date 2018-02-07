/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.table;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.network.instance.L3VrfWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ProtocolsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnection;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.TableConnectionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.table.connections.table.connection.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.ADDRESSFAMILY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.IPV6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.INSTALLPROTOCOLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpTableConnectionWriter implements
        L3VrfWriter<Config> {

    private static final String UPDATE_REDIS = "configure terminal\n" +
            "router bgp {$bgp}\n" +
            "address-family {$family}" +
            "{.if ($vrf) } vrf {$vrf}{/if}" +
            "\n" +
            "{.if ($add) }{.else}no {/if}" +
            "redistribute {$protocol} {$protocol_id}" +
            "{.if ($vrf) } vrf {$vrf}{/if}" +
            "{.if ($add) }{.if ($policy) } route-map {$policy}{/if}{/if}" +
            "\n" +
            "end";

    private Cli cli;

    public BgpTableConnectionWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier,
                                              Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        if (config.getDstProtocol().equals(BGP.class)) {
            List<Protocol> allProtocols = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE).child(Protocols.class))
                    .or(new ProtocolsBuilder().setProtocol(Collections.emptyList()).build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier().equals(BGP.class))
                    .collect(Collectors.toList());

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForBgp(instanceIdentifier, dstProtocol, config, allProtocols, true);
            }
        }
    }

    private void writeCurrentAttributesForBgp(InstanceIdentifier<Config> id,
                                              Protocol bgpProtocol,
                                              Config config,
                                              List<Protocol> protocols,
                                              boolean add) throws WriteFailedException.CreateFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        TableConnectionKey tableConnectionKey = id.firstKeyOf(TableConnection.class);

        Preconditions.checkArgument(config.getSrcProtocol().equals(OSPF.class),
                "Unable to redistribute from: %s protocol, not supported", config.getSrcProtocol());

        List<Protocol> srcProtocols = protocols.stream()
                .filter(p -> p.getIdentifier().equals(config.getSrcProtocol()))
                .collect(Collectors.toList());

        Preconditions.checkArgument(!srcProtocols.isEmpty(),
                "No protocols: %s configured in current network", config.getSrcProtocol());

        List<String> importPolicy = config.getImportPolicy() == null ? Collections.emptyList() : config.getImportPolicy();

        Preconditions.checkArgument(importPolicy.isEmpty() || importPolicy.size() ==1,
                "Only a single import policy is supported: %s", importPolicy);

        for (Protocol srcProto : srcProtocols) {
            blockingWriteAndRead(cli, id, config,
                    fT(UPDATE_REDIS,
                            "bgp", bgpProtocol.getBgp().getGlobal().getConfig().getAs().getValue(),
                            "family", toDeviceAddressFamily(tableConnectionKey.getAddressFamily()),
                            "add", add ? true : null,
                            "protocol", toDeviceProtocol(srcProto.getIdentifier()),
                            "protocol_id", srcProto.getName(),
                            "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                            "policy", importPolicy.isEmpty() ? null : importPolicy.get(0)));
        }
    }

    private static String toDeviceAddressFamily(Class<? extends ADDRESSFAMILY> afiSafiName) {
        if (afiSafiName.equals(IPV4.class)) {
            return "ipv4";
        } else if (afiSafiName.equals(IPV6.class)) {
            return "ipv6";
        } else {
            throw new IllegalArgumentException("Unsupported address family type: " + afiSafiName);
        }
    }

    private String toDeviceProtocol(Class<? extends INSTALLPROTOCOLTYPE> identifier) {
        if (identifier.equals(OSPF.class)) {
            return "ospf";
        }
        if (identifier.equals(BGP.class)) {
            return "bgp";
        }

        throw new IllegalArgumentException("Protocol of type: " + identifier + " is not supported");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id,
                                               Config dataBefore,
                                               Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier,
                                               Config config,
                                               WriteContext writeContext) throws WriteFailedException {
        if (config.getDstProtocol().equals(BGP.class)) {
            List<Protocol> allProtocols = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, IIDs.NE_NETWORKINSTANCE).child(Protocols.class))
                    .or(new ProtocolsBuilder().setProtocol(Collections.emptyList()).build())
                    .getProtocol();

            List<Protocol> dstProtocols = allProtocols.stream()
                    .filter(p -> p.getIdentifier().equals(BGP.class))
                    .collect(Collectors.toList());

            for (Protocol dstProtocol : dstProtocols) {
                writeCurrentAttributesForBgp(instanceIdentifier, dstProtocol, config, allProtocols, false);
            }
        }
    }
}

/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.bgp.handler.local.aggregates;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalConfigWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.aggregate.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpLocalAggregateConfigWriter implements BgpWriter<Config> {

    private final Cli cli;

    private static final String ENTER_AFI_SAFI = "{% if ($afi_safi) %}" +
            "address-family {$afi_safi}" +
            "{% if ($vrf) %}" +
            " vrf {$vrf}" +
            "{% else %}" +
            "{% endif %}" +
            "\n" +
            "{% endif %}";

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "router bgp {$as}\n" +

            // Enter afi + VRF
            ENTER_AFI_SAFI +

            "network {$network} {.if ($mask) }mask {$mask}{/if}\n" +
            "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
            "router bgp {$as}\n" +

            // Enter afi + VRF
            ENTER_AFI_SAFI +

            "no network {$network} {.if ($mask) }mask {$mask}{/if}\n" +
            "end";

    public BgpLocalAggregateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        final Protocols networkInstance =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class).child(Protocols.class)).get();
        Optional<Bgp> bgp = getBgpGlobal(networkInstance);
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        Preconditions.checkArgument(bgp.isPresent(),
                "BGP not configured for VRF: %s. Cannot configure networks", vrfKey.getName());

        Set<AfiSafi> afiSafis = GlobalConfigWriter.getAfiSafis(bgp.orElse(null));

        if (afiSafis.isEmpty()) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                    fT(WRITE_TEMPLATE,
                            "as", bgp.get().getGlobal().getConfig().getAs().getValue(),
                            "network", getNetAddress(config.getPrefix()),
                            "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                            "mask", getNetMask(config.getPrefix())));
        } else {
            for (AfiSafi afiSafi : afiSafis) {
                blockingWriteAndRead(cli, instanceIdentifier, config,
                        fT(WRITE_TEMPLATE,
                                "as", bgp.get().getGlobal().getConfig().getAs().getValue(),
                                "network", getNetAddress(config.getPrefix()),
                                "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiSafi.getAfiSafiName()),
                                "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                                "mask", getNetMask(config.getPrefix())));
            }
        }
    }

    private String getNetAddress(@Nonnull IpPrefix ipPrefix) {
        if (ipPrefix.getIpv4Prefix() != null) {
            SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
            return utils.getInfo().getNetworkAddress();
        } else {
            return ipPrefix.getIpv6Prefix().getValue();
        }
    }

    @Nullable
    private String getNetMask(@Nonnull IpPrefix ipPrefix) {
        if (ipPrefix.getIpv4Prefix() != null) {
            SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
            return utils.getInfo().getNetmask();
        } else {
            return null;
        }
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
                                               WriteContext writeContext) throws WriteFailedException {
        final Protocols networkInstance =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, NetworkInstance.class).child(Protocols.class)).get();
        Optional<Bgp> bgp = getBgpGlobal(networkInstance);
        NetworkInstanceKey vrfKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);

        Set<AfiSafi> afiSafis = GlobalConfigWriter.getAfiSafis(bgp.orElse(null));

        if (afiSafis.isEmpty()) {
            blockingDeleteAndRead(cli, instanceIdentifier,
                    fT(DELETE_TEMPLATE,
                            "as", bgp.get().getGlobal().getConfig().getAs().getValue(),
                            "network", getNetAddress(config.getPrefix()),
                            "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                            "mask", getNetMask(config.getPrefix())));
        } else {
            for (AfiSafi afiSafi : afiSafis) {
                blockingDeleteAndRead(cli, instanceIdentifier,
                        fT(DELETE_TEMPLATE,
                                "as", bgp.get().getGlobal().getConfig().getAs().getValue(),
                                "network", getNetAddress(config.getPrefix()),
                                "afi_safi", GlobalAfiSafiConfigWriter.toDeviceAddressFamily(afiSafi.getAfiSafiName()),
                                "vrf", vrfKey.equals(NetworInstance.DEFAULT_NETWORK) ? null : vrfKey.getName(),
                                "mask", getNetMask(config.getPrefix())));
            }
        }
    }

    private Optional<Bgp> getBgpGlobal(Protocols protocolsContainer) {
        List<Protocol> protocols = protocolsContainer.getProtocol();
        if (protocols != null && !protocols.isEmpty()) {
            Optional<Protocol> bgpProtocol =
                    protocols.stream().filter(protocol -> protocol.getIdentifier().equals(BGP.class)).findFirst();
            if (bgpProtocol.isPresent()) {
                return Optional.ofNullable(bgpProtocol.get().getBgp());
            }
        }
        return Optional.empty();
    }
}

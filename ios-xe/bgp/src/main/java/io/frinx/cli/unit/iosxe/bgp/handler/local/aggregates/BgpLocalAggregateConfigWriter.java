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

package io.frinx.cli.unit.iosxe.bgp.handler.local.aggregates;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.iosxe.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.net.util.SubnetUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class BgpLocalAggregateConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    private static final String ENTER_AFI_SAFI = "{% if ($afi_safi) %}"
            + "address-family {$afi_safi}"
            + "{% if ($vrf) %}"
            + " vrf {$vrf}"
            + "{% else %}"
            + "{% endif %}"
            + "\n"
            + "{% endif %}";

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "router bgp {$as}\n"
            +

            // Enter afi + VRF
            ENTER_AFI_SAFI
            +

            "network {$network} {.if ($mask) }mask {$mask}{/if}\n"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "router bgp {$as}\n"
            +

            // Enter afi + VRF
            ENTER_AFI_SAFI
            +

            "no network {$network} {.if ($mask) }mask {$mask}{/if}\n"
            + "end";

    public BgpLocalAggregateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final Protocols networkInstance =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class).child(Protocols
                        .class)).get();
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

    private String getNetAddress(@NotNull IpPrefix ipPrefix) {
        if (ipPrefix.getIpv4Prefix() != null) {
            SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
            return utils.getInfo().getNetworkAddress();
        } else {
            return ipPrefix.getIpv6Prefix().getValue();
        }
    }

    @Nullable
    private String getNetMask(@NotNull IpPrefix ipPrefix) {
        if (ipPrefix.getIpv4Prefix() != null) {
            SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
            return utils.getInfo().getNetmask();
        } else {
            return null;
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        // this is fine, we manipulate with networks and masks only, and there can be multiple masks
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final Protocols networkInstance =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, NetworkInstance.class).child(Protocols
                        .class)).get();
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
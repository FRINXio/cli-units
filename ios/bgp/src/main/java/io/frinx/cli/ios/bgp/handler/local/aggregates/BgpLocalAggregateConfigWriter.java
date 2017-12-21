/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.local.aggregates;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apache.commons.net.util.SubnetUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local.aggregate.top.local.aggregates.aggregate.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;

public class BgpLocalAggregateConfigWriter implements BgpWriter<Config> {

    private final Cli cli;

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
        "router bgp %s\n" +
        "address-family ipv4 vrf %s\n" +
        "network %s mask %s\n" +
        "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
        "router bgp %s\n" +
        "address-family ipv4 vrf %s\n" +
        "no network %s mask %s\n" +
        "end";

    public BgpLocalAggregateConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        final NetworkInstance networkInstance =
            writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();
        Optional<Global> bgpGlobal = getBgpGlobal(networkInstance);

        if (bgpGlobal.isPresent()) {
            blockingWriteAndRead(cli, instanceIdentifier, config,
                f(WRITE_TEMPLATE,
                    bgpGlobal.get().getConfig().getAs().getValue(),
                    networkInstance.getName(),
                    getNetAddress(config.getPrefix()),
                    getNetMask(config.getPrefix())));
        }
    }

    private String getNetAddress(@Nonnull IpPrefix ipPrefix) {
        SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
        return utils.getInfo().getNetworkAddress();
    }

    private String getNetMask(@Nonnull IpPrefix ipPrefix) {
        SubnetUtils utils = new SubnetUtils(ipPrefix.getIpv4Prefix().getValue());
        return utils.getInfo().getNetmask();
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        final NetworkInstance networkInstance =
            writeContext.readBefore(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();
        Optional<Global> bgpGlobal = getBgpGlobal(networkInstance);

        if (bgpGlobal.isPresent()) {
            blockingDeleteAndRead(cli, instanceIdentifier,
                f(DELETE_TEMPLATE,
                    bgpGlobal.get().getConfig().getAs().getValue(),
                    networkInstance.getName(),
                    getNetAddress(config.getPrefix()),
                    getNetMask(config.getPrefix())));
        }
    }

    private Optional<Global> getBgpGlobal(NetworkInstance networkInstance) {
        List<Protocol> protocols = networkInstance.getProtocols().getProtocol();
        if (protocols != null && !protocols.isEmpty()) {
            Optional<Protocol> bgpProtocol =
                protocols.stream().filter(protocol -> protocol.getIdentifier().equals(BGP.class)).findFirst();
            if (bgpProtocol.isPresent()) {
                return Optional.ofNullable(bgpProtocol.get().getBgp().getGlobal());
            }
        }
        return Optional.empty();
    }
}

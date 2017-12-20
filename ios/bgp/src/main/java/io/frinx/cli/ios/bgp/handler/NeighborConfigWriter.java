/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import com.google.common.base.Preconditions;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;

public class NeighborConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        final Global global = writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Bgp.class)).get().getGlobal();
        final NetworkInstance networkInstance =
            writeContext.readAfter(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();
        Preconditions.checkNotNull(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address());
        String ipV4Address = getIpv4Value(instanceIdentifier);

        blockingWriteAndRead(cli, instanceIdentifier, config, "configure terminal",
            f("router bgp %s", getAsValue(global)),
            f("address-family ipv4 vrf %s", networkInstance.getName()),
            f("neighbor %s remote-as %s", ipV4Address, getPeerAsValue(config)),
            f("neighbor %s activate", ipV4Address),
            "end");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config config,
        WriteContext writeContext) throws WriteFailedException {
        final Global global = writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Bgp.class)).get().getGlobal();
        final NetworkInstance networkInstance =
            writeContext.readBefore(RWUtils.cutId(instanceIdentifier, NetworkInstance.class)).get();
        String ipV4Address = getIpv4Value(instanceIdentifier);

        blockingDeleteAndRead(cli, instanceIdentifier, "configure terminal",
            f("router bgp %s", getAsValue(global)),
            f("address-family ipv4 vrf %s", networkInstance.getName()),
            f("no neighbor %s remote-as %s", ipV4Address, getPeerAsValue(config)),
            f("no neighbor %s activate", ipV4Address),
            "end");
    }

    private String getIpv4Value(InstanceIdentifier<Config> instanceIdentifier) {
        return Preconditions.checkNotNull(
            Preconditions.checkNotNull(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress())
                .getIpv4Address()).getValue();
    }

    private Long getPeerAsValue(Config config) {
        return Preconditions.checkNotNull(config.getPeerAs()).getValue();
    }

    private Long getAsValue(Global global) {
        return Preconditions.checkNotNull(Preconditions.checkNotNull(global.getConfig()).getAs()).getValue();
    }
}

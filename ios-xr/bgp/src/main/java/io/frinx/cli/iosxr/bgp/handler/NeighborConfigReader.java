/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class NeighborConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {

    // show run router bgp 65555 instance bla vrf b neighbor 192.168.1.1
    private static final String SH_NEI = "show run router bgp %s %s %s neighbor %s";
    private static final Pattern SHUTDOWN_LINE = Pattern.compile("shutdown");
    private static final Pattern REMOTE_AS_LINE = Pattern.compile("remote-as (?<remoteAs>.+)");
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("use neighbor-group (?<group>.+)");

    private Cli cli;

    public NeighborConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public ConfigBuilder getBuilder(@Nonnull InstanceIdentifier<Config> instanceIdentifier) {
        return new ConfigBuilder();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((NeighborBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
            .child(Global.class)
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config.class))
            .orNull();

        if (globalConfig == null) {
            return;
        }

        IpAddress neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress();
        configBuilder.setNeighborAddress(neighborIp);

        String address = neighborIp.getIpv4Address().getValue();
        String insName = instanceIdentifier.firstKeyOf(Protocol.class).getName().equals(NetworInstance.DEFAULT_NETWORK_NAME) ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class).getName();
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName().equals(NetworInstance.DEFAULT_NETWORK_NAME) ?
                "" : "vrf " + instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        String output = blockingRead(String.format(SH_NEI, globalConfig.getAs().getValue().intValue(), insName, vrfName, address), cli, instanceIdentifier, readContext);

        readNeighbor(output, configBuilder);
    }

    @VisibleForTesting
    public static void readNeighbor(final String output, final ConfigBuilder configBuilder) {
        // remote-as 65000
        ParsingUtils.parseField(output.trim(), 0,
                REMOTE_AS_LINE::matcher,
                matcher -> matcher.group("remoteAs"),
                value -> configBuilder.setPeerAs(new AsNumber(Long.parseLong(value.trim()))));

        // shutdown (reverse the result, if we DO find the match, set to FALSE)
        ParsingUtils.findMatch(output, SHUTDOWN_LINE, configBuilder::setEnabled);
        configBuilder.setEnabled(!configBuilder.isEnabled());

        // use neighbor-group iBGP
        ParsingUtils.parseField(output, NEIGHBOR_LINE::matcher, matcher -> matcher.group("group"), configBuilder::setPeerGroup);
    }
}

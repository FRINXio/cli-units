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

package io.frinx.cli.unit.iosxe.bgp.handler.peergroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborTransportConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.transport.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.transport.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupTransportConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public PeerGroupTransportConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String peerGroupName = instanceIdentifier.firstKeyOf(PeerGroup.class).getPeerGroupName();

        String output = blockingRead(String.format(NeighborConfigReader.SH_SUMM, peerGroupName), cli,
                instanceIdentifier,
                readContext);
        parseConfigAttributes(output, configBuilder, vrfName);

    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, @Nonnull ConfigBuilder configBuilder, String vrfName) {
        // Reuse NeighboTransportConfigReader to parse the fields
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport
                .ConfigBuilder neighborBuilder =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base
                        .transport.ConfigBuilder();
        NeighborTransportConfigReader.parseConfigAttributes(output, neighborBuilder, vrfName);
        configBuilder.fieldsFrom(neighborBuilder.build());
    }
}

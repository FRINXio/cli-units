/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.ios.bgp.handler.peergroup;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public PeerGroupConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                             @NotNull ConfigBuilder configBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String peerGroupName = instanceIdentifier.firstKeyOf(PeerGroup.class).getPeerGroupName();
        configBuilder.setPeerGroupName(peerGroupName);

        String output = blockingRead(String.format(NeighborConfigReader.SH_SUMM, peerGroupName), cli,
                instanceIdentifier, readContext);
        parseConfigAttributes(output, configBuilder, vrfName);
    }

    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder
                neighborBuilder =
                new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base
                        .ConfigBuilder();
        // Reuse NeighboConfigReader to parse the fields
        NeighborConfigReader.parseConfigAttributes(output, neighborBuilder, vrfName);
        configBuilder.fieldsFrom(neighborBuilder.build());
    }
}
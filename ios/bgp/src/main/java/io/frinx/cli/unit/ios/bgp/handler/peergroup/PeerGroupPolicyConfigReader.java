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
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Arrays;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public PeerGroupPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                             @NotNull ConfigBuilder configBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String peerGroupId = PeerGroupWriter.getPeerGroupId(id);

        String output = blockingRead(String.format(NeighborConfigReader.SH_SUMM, peerGroupId), cli, id, readContext);
        String[] outputSplit = NeighborReader.splitOutput(output);
        Stream<String> outputStream = Arrays.stream(outputSplit)
                .filter(line -> !line.contains("address-family"));

        NeighborPolicyConfigReader.parseConfigAttributes(configBuilder, vrfKey.getName(), outputStream);
    }
}
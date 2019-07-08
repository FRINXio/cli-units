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
import io.frinx.cli.unit.ios.bgp.handler.GlobalAfiSafiConfigWriter;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborPolicyConfigReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Arrays;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupAfiSafiPolicyConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public PeerGroupAfiSafiPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String peerGroupId = PeerGroupWriter.getPeerGroupId(instanceIdentifier);
        String afiSafi = GlobalAfiSafiConfigWriter.toDeviceAddressFamily(instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName());

        String output = blockingRead(String.format(NeighborConfigReader.SH_SUMM, peerGroupId), cli,
                instanceIdentifier, readContext);
        String[] outputLines = NeighborReader.splitOutput(output);
        Stream<String> outputStream = Arrays.stream(outputLines)
                .filter(line -> line.contains("address-family " + afiSafi));

        NeighborPolicyConfigReader.parseConfigAttributes(configBuilder, vrfName, outputStream);
    }
}

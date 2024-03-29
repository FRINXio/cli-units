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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.ios.bgp.handler.neighbor.NeighborReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupReader implements CliConfigListReader<PeerGroup, PeerGroupKey, PeerGroupBuilder> {

    private static final Pattern GROUP_LINE = Pattern.compile("neighbor (?<id>\\S+) peer-group");
    private Cli cli;

    public PeerGroupReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<PeerGroupKey> getAllIds(@NotNull InstanceIdentifier<PeerGroup> instanceIdentifier,
                                               @NotNull ReadContext readContext) throws ReadFailedException {

        String networkInstanceName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(networkInstanceName)) {
            return getDefaultPeerGroupKeys(blockingRead(NeighborReader.SH_SUMM, cli, instanceIdentifier, readContext));
        } else {
            return getVrfPeerGroupKeys(blockingRead(NeighborReader.SH_SUMM, cli, instanceIdentifier, readContext),
                    networkInstanceName);
        }
    }

    @VisibleForTesting
    static List<PeerGroupKey> getVrfPeerGroupKeys(String output, String vrfName) {
        return NeighborReader.parseKeys(Arrays.stream(NeighborReader.splitOutput(output))
                        .filter(value -> value.contains(vrfName))
                        .findFirst(),
                PeerGroupKey::new, GROUP_LINE);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<PeerGroup> instanceIdentifier,
                                             @NotNull PeerGroupBuilder peerGroupBuilder, @NotNull ReadContext
                                                         readContext) throws ReadFailedException {
        peerGroupBuilder.setPeerGroupName(instanceIdentifier.firstKeyOf(PeerGroup.class).getPeerGroupName());
    }

    @VisibleForTesting
    static List<PeerGroupKey> getDefaultPeerGroupKeys(String output) {
        return NeighborReader.parseKeys(Arrays.stream(NeighborReader.splitOutput(output))
                        .filter(value -> !value.contains("vrf"))
                        .findFirst(),
                PeerGroupKey::new, GROUP_LINE);
    }
}
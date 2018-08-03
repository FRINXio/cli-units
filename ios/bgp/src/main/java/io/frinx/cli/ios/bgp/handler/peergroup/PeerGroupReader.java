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

package io.frinx.cli.ios.bgp.handler.peergroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.bgp.handler.BgpProtocolReader;
import io.frinx.cli.ios.bgp.handler.neighbor.NeighborReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.PeerGroupsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupReader implements BgpListReader.BgpConfigListReader<PeerGroup, PeerGroupKey, PeerGroupBuilder> {

    private static final Pattern GROUP_LINE = Pattern.compile("neighbor (?<id>\\S+) peer-group");
    private Cli cli;

    public PeerGroupReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<PeerGroupKey> getAllIdsForType(@Nonnull InstanceIdentifier<PeerGroup> instanceIdentifier,
                                               @Nonnull ReadContext readContext) throws ReadFailedException {

        String networkInstanceName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(networkInstanceName)) {
            return getDefaultPeerGroupKeys(blockingRead(NeighborReader.SH_SUMM, cli, instanceIdentifier, readContext));
        } else {
            return getVrfPeerGroupKeys(blockingRead(NeighborReader.SH_SUMM, cli, instanceIdentifier, readContext),
                    networkInstanceName);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<PeerGroup> list) {
        ((PeerGroupsBuilder) builder).setPeerGroup(list).build();
    }

    @VisibleForTesting
    static List<PeerGroupKey> getVrfPeerGroupKeys(String output, String vrfName) {
        return NeighborReader.parseKeys(Arrays.stream(NeighborReader.splitOutput(output))
                        .filter(value -> value.contains(vrfName))
                        .findFirst(),
                PeerGroupKey::new, GROUP_LINE);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<PeerGroup> instanceIdentifier,
                                             @Nonnull PeerGroupBuilder peerGroupBuilder, @Nonnull ReadContext
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

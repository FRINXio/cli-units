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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import io.frinx.cli.unit.iosxe.bgp.handler.neighbor.NeighborReaderTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;

class PeerGroupReaderTest {

    @Test
    void testNeighborIds() {
        List<PeerGroupKey> keys = PeerGroupReader.getDefaultPeerGroupKeys(NeighborReaderTest.SUMM_OUTPUT_NEIGHBORS);
        assertArrayEquals(new String[]{"abcd"},
                keys.stream().map(PeerGroupKey::getPeerGroupName).toArray());

        keys = PeerGroupReader.getVrfPeerGroupKeys(NeighborReaderTest.SUMM_OUTPUT_NEIGHBORS, "vrf1");
        assertArrayEquals(new String[]{"abcdVRF", "abcdVRF2"},
                keys.stream().map(PeerGroupKey::getPeerGroupName).toArray());
    }
}
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

import static io.frinx.cli.ios.bgp.handler.neighbor.NeighborReaderTest.SUMM_OUTPUT_NEIGHBORS;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroupKey;

public class PeerGroupReaderTest {

    @Test
    public void testNeighborIds() {
        List<PeerGroupKey> keys = PeerGroupReader.getDefaultPeerGroupKeys(SUMM_OUTPUT_NEIGHBORS);
        Assert.assertArrayEquals(new String[]{"abcd"},
                keys.stream().map(PeerGroupKey::getPeerGroupName).toArray());

        keys = PeerGroupReader.getVrfPeerGroupKeys(SUMM_OUTPUT_NEIGHBORS, "vrf1");
        Assert.assertArrayEquals(new String[]{"abcdVRF", "abcdVRF2"},
                keys.stream().map(PeerGroupKey::getPeerGroupName).toArray());
    }
}
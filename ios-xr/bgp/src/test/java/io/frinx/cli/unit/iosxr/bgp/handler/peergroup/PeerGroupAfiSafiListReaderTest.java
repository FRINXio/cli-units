/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;

class PeerGroupAfiSafiListReaderTest {

    private static final String OUTPUT = """
            address-family ipv4 unicast
            address-family vpnv4 unicast
            """;

    private PeerGroupAfiSafiListReader reader;

    @BeforeEach
    void setUp() {
        reader = new PeerGroupAfiSafiListReader(Mockito.mock(Cli.class));
    }

    @Test
    void testGetAllIds() {
        List<AfiSafiKey> result = reader.parseAllIds(OUTPUT);
        assertEquals(2, result.size());
        assertThat(new HashSet<>(result),
                CoreMatchers.equalTo(Sets.newSet(new AfiSafiKey(IPV4UNICAST.class),
                        new AfiSafiKey(L3VPNIPV4UNICAST.class))));
    }
}
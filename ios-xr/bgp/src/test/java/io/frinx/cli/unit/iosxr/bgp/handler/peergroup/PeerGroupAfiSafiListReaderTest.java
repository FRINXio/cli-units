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

import io.frinx.cli.io.Cli;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;

public class PeerGroupAfiSafiListReaderTest {

    private static final String OUTPUT = "address-family ipv4 unicast\n"
            + "address-family vpnv4 unicast\n";

    private PeerGroupAfiSafiListReader reader;

    @Before
    public void setUp() {
        reader = new PeerGroupAfiSafiListReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testGetAllIds() {
        List<AfiSafiKey> result = reader.parseAllIds(OUTPUT);
        Assert.assertEquals(2, result.size());
        Assert.assertThat(new HashSet<>(result),
                CoreMatchers.equalTo(Sets.newSet(new AfiSafiKey(IPV4UNICAST.class),
                        new AfiSafiKey(L3VPNIPV4UNICAST.class))));
    }
}
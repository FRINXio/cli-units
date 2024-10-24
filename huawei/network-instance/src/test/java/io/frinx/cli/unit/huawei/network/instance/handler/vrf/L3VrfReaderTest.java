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

package io.frinx.cli.unit.huawei.network.instance.handler.vrf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class L3VrfReaderTest {
    private static final String DISPLAY_L3VRF = """
            #
            ip vpn-instance 3940
             ipv4-family
              route-distinguisher 2:2
             ipv6-family
              route-distinguisher 2:2
            #
            ip vpn-instance CASA_3008_IPVPN_2
             ipv4-family
            #
            ip vpn-instance MANAGEMENT
             ipv4-family
              route-distinguisher 1:1
            #
            ip vpn-instance UBEE
             ipv4-family
              route-distinguisher 1:11
             ipv6-family
              route-distinguisher 1:11
            #
            ip vpn-instance UBEEipv4-family
            #
            ip vpn-instance VLAN271752
             ipv4-family                             \s
              route-distinguisher 198.18.100.5:100   \s
              prefix limit 100 80                    \s
            #                                        \s
            return      \
            """;

    private static final List<NetworkInstanceKey> IDS_EXPECTED = Lists.newArrayList("3940",
            "CASA_3008_IPVPN_2", "MANAGEMENT", "UBEE", "VLAN271752", "default").stream()
            .map(NetworkInstanceKey::new)
            .collect(Collectors.toList());

    @Mock
    CliReader reader;

    @Mock
    InstanceIdentifier<?> id;

    @Mock
    ReadContext ctx;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when((ctx).getModificationCache()).thenReturn(new ModificationCache());
        Mockito.when(reader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.eq(id), Mockito.any(ReadContext.class))).thenReturn(DISPLAY_L3VRF);
    }

    @Test
    void testReader() throws ReadFailedException {
        assertEquals(IDS_EXPECTED, new L3VrfReader(Mockito.mock(Cli.class)).getAllIds(reader, id, ctx));
    }
}

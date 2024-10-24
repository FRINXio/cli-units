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

package io.frinx.cli.unit.ios.bgp.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;

class GlobalAfiSafiReaderTest {

    private static final String BGP_OUTPUT = """
             address-family vpnv4\r
             address-family vpnv6\r
            router bgp 65000\r
             address-family ipv4\r
             address-family ipv4 vrf a
            """;

    @Test
    void testParse() throws Exception {
        List<AfiSafiKey> afamilies = GlobalAfiSafiReader.getAfiKeys(BGP_OUTPUT, "a");
        List<AfiSafiKey> bfamilies = GlobalAfiSafiReader.getAfiKeys(BGP_OUTPUT, "b");
        List<AfiSafiKey> defaultFamilies = GlobalAfiSafiReader.getDefaultAfiKeys(BGP_OUTPUT);
        AfiSafiKey ipv4Key = new AfiSafiKey(IPV4UNICAST.class);
        assertThat(afamilies, CoreMatchers.hasItems(ipv4Key));
        assertTrue(bfamilies.isEmpty());
        assertEquals(1, defaultFamilies.size());
        assertThat(defaultFamilies, CoreMatchers.hasItems(ipv4Key));

    }
}


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

package io.frinx.cli.unit.huawei.bgp.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpGlobalConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;

public class GlobalConfigReaderTest {

    public static final String OUTPUT_1 = """
            bgp 65222
             router-id 198.18.100.5\r
              ipv4-family vpn-instance VLAN271752\r
              import-route static\r
             ipv4-family vpn-instance VLAN278899 router-id 198.18.100.90\r
              import-route static\r
              import-route direct""";

    public static final String OUTPUT_2 = """
            bgp 65222
             router-id 198.18.100.5\r
              import-route direct\r
              import-route static\r
             ipv4-family vpn-instance VLAN271752\r
              import-route direct\r
              import-route static""";


    @Test
    void testDefaultNetworkParse1() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(OUTPUT_1, configBuilder, NetworInstance.DEFAULT_NETWORK);
        assertEquals(new ConfigBuilder()
            .setAs(new AsNumber(65222L))
            .setRouterId(new DottedQuad("198.18.100.5"))
            .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                    .setImportRoute(Collections.emptyList())
                    .build())
            .build(), configBuilder.build());
    }

    @Test
    void testDefaultNetworkParse2() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(OUTPUT_2, configBuilder, NetworInstance.DEFAULT_NETWORK);
        assertEquals(new ConfigBuilder()
                .setAs(new AsNumber(65222L))
                .setRouterId(new DottedQuad("198.18.100.5"))
                .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                        .setImportRoute(Arrays.asList("direct", "static"))
                        .build())
                .build(), configBuilder.build());
    }

    @Test
    void testVrfParse1() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(OUTPUT_1, configBuilder, new NetworkInstanceKey("VLAN271752"));
        assertEquals(new ConfigBuilder()
            .setAs(new AsNumber(65222L))
            .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                .setImportRoute(Collections.singletonList("static"))
                .build())
            .build(), configBuilder.build());
    }

    @Test
    void testVrfParse2() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(OUTPUT_1, configBuilder, new NetworkInstanceKey("VLAN278899"));
        assertEquals(new ConfigBuilder()
                .setAs(new AsNumber(65222L))
                .setRouterId(new DottedQuad("198.18.100.90"))
                .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                        .setImportRoute(Arrays.asList("static", "direct"))
                        .build())
                .build(), configBuilder.build());
    }

    @Test
    void testVrfParse3() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        GlobalConfigReader.parseConfigAttributes(OUTPUT_2, configBuilder, new NetworkInstanceKey("VLAN271752"));
        assertEquals(new ConfigBuilder()
                .setAs(new AsNumber(65222L))
                .addAugmentation(BgpGlobalConfigAug.class, new BgpGlobalConfigAugBuilder()
                        .setImportRoute(Arrays.asList("direct", "static"))
                        .build())
                .build(), configBuilder.build());
    }
}

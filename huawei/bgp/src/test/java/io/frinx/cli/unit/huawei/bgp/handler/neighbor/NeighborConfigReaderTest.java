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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigExtension.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.bgp.neighbor.config.extension.TimerConfigurationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class NeighborConfigReaderTest {

    public static final String OUTPUT_WITHOUT_AUG_COMMANDS = """
             peer 217.105.224.9 as-number 6830
             peer 217.105.224.9 description main1
             peer 217.105.224.9 route-policy RP-IPVPN-PRIMARY-PE import
             peer 217.105.224.9 route-policy RP-IPVPN-PRIMARY-CPE-PRIMARY-PE export
            """;


    public static final String OUTPUT = OUTPUT_WITHOUT_AUG_COMMANDS
        + " peer 217.105.224.9 timer keepalive 10 hold 30\n"
        + " peer 217.105.224.9 password cipher %^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#\n"
        + " peer 217.105.224.9 path-mtu auto-discovery\n";

    @Test
    void testOutputWithoutAugCommandsParse() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NeighborConfigReader.parseConfigAttributes(OUTPUT_WITHOUT_AUG_COMMANDS, configBuilder,
                NetworInstance.DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                .setDescription("main1")
                .setPeerAs(new AsNumber(6830L))
                .build(), configBuilder.build());
    }

    @Test
    void testParse() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NeighborConfigReader.parseConfigAttributes(OUTPUT , configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        assertEquals(new ConfigBuilder()
                .setDescription("main1")
                .setAuthPassword(new EncryptedPassword(new PlainString(
                                "%^%#kJb)3DSFJ9&&5q0mEo[&ahUIPl(_nUJ<2]%SS|97*LZQ6H\\_3S5n1O-8xm69%^%#")))
                .setPeerAs(new AsNumber(6830L))
                .addAugmentation(BgpNeighborConfigAug.class, new BgpNeighborConfigAugBuilder()
                        .setTimerConfiguration(new TimerConfigurationBuilder()
                                .setTimerMode("keepalive")
                                .setTimeBefore((short) 10)
                                .setTimeAfter((short) 30).build())
                        .setTransport(Transport.AutoDiscovery)
                        .build())
                .build(), configBuilder.build());
    }
}

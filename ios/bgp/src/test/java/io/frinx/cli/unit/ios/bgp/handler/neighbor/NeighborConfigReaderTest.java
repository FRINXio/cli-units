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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.VERSION4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.CommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class NeighborConfigReaderTest {

    public static final String OUTPUT_WITHOUT_AUG_COMMANDS = "router bgp 65000\n"
            + " neighbor 1.2.3.4 remote-as 45\n"
            + " neighbor 1.2.3.4 password 7 AB7657E89DG\n"
            + " neighbor 1.2.3.4 peer-group group12\n"
            + " neighbor 1.2.3.4 description description\n"
            + " neighbor 1.2.3.4 send-community both\n"
            + " neighbor 1.2.3.4 activate\n";

    public static final String OUTPUT = OUTPUT_WITHOUT_AUG_COMMANDS
            + " neighbor 1.2.3.4 as-override\n"
            + " neighbor 1.2.3.4 version 4\n";

    @Test
    public void testOutputWithoutAugCommandsParse() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NeighborConfigReader.parseConfigAttributes(OUTPUT_WITHOUT_AUG_COMMANDS, configBuilder,
            NetworInstance.DEFAULT_NETWORK_NAME);
        Assert.assertEquals(new ConfigBuilder()
                        .setDescription("description")
                        .setAuthPassword(new EncryptedPassword(new EncryptedString("Encrypted[7 AB7657E89DG]")))
                        .setPeerAs(new AsNumber(45L))
                        .setPeerGroup("group12")
                        .setEnabled(true)
                        .setSendCommunity(CommunityType.BOTH)
                        .build(),
                configBuilder.build());
    }

    @Test
    public void testParse() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        NeighborConfigReader.parseConfigAttributes(OUTPUT, configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        Assert.assertEquals(new ConfigBuilder()
                        .setDescription("description")
                        .setAuthPassword(new EncryptedPassword(new EncryptedString("Encrypted[7 AB7657E89DG]")))
                        .setPeerAs(new AsNumber(45L))
                        .setPeerGroup("group12")
                        .setEnabled(true)
                        .setSendCommunity(CommunityType.BOTH)
                        .addAugmentation(BgpNeighborConfigAug.class, new BgpNeighborConfigAugBuilder()
                                .setNeighborVersion(VERSION4.class)
                                .setAsOverride(true)
                                .build())
                        .build(),
                configBuilder.build());
    }
}

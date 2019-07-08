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

package io.frinx.cli.unit.ios.bgp.handler.peergroup;

import io.frinx.openconfig.network.instance.NetworInstance;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;

public class PeerGroupConfigReaderTest {

    private static final String OUTPUT = "router bgp 65000\n"
            + " neighbor G-1 peer-group\n"
            + " neighbor G-1 remote-as 45\n"
            + " neighbor G-1 password passwd\n"
            + " neighbor G-1 description description\n"
            +
            // These 2 are neighbor specific and should be ignored by group parser
            " neighbor G-1 peer-group abcd\n"
            + " neighbor G-1 activate\n";

    @Test
    public void testParse() throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder();
        PeerGroupConfigReader.parseConfigAttributes(OUTPUT, configBuilder, NetworInstance.DEFAULT_NETWORK_NAME);
        Assert.assertEquals(new ConfigBuilder()
                        .setDescription("description")
                        .setAuthPassword(new EncryptedPassword(new PlainString("passwd")))
                        .setPeerAs(new AsNumber(45L))
                        .build(),
                configBuilder.build());
    }
}
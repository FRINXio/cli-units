/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.network.instance.handler.vlan;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig.Status;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;

public class VlanConfigReaderTest {

    private static final String OUTPUT_UP = "vlan 12\n"
            + " name up\n"
            + "end\n";

    private static final Config EXPECTED_UP = new ConfigBuilder()
            .setVlanId(new VlanId(12))
            .setName("up")
            .setStatus(Status.ACTIVE)
            .build();

    private static final String OUTPUT_DOWN = "vlan 34\n"
            + " name down\n"
            + " shutdown\n"
            + "end\n";

    private static final Config EXPECTED_DOWN = new ConfigBuilder()
            .setVlanId(new VlanId(34))
            .setName("down")
            .setStatus(Status.SUSPENDED)
            .build();

    @Test
    public void test() {
        ConfigBuilder configBuilder = new ConfigBuilder();

        VlanConfigReader.parseVlanConfig(OUTPUT_UP, configBuilder, new VlanId(12));
        Assert.assertEquals(EXPECTED_UP, configBuilder.build());

        VlanConfigReader.parseVlanConfig(OUTPUT_DOWN, configBuilder, new VlanId(34));
        Assert.assertEquals(EXPECTED_DOWN, configBuilder.build());
    }

}
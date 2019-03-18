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

package io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4;

import io.frinx.cli.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;

public final class Ipv4ConfigReaderTest {

    private static final String DISPLAY_IP_INT_BR_OUTPUT = "*down: administratively down\n"
            + "!down: FIB overload down\n"
            + "^down: standby\n"
            + "(l): loopback\n"
            + "(s): spoofing\n"
            + "(d): Dampening Suppressed\n"
            + "(E): E-Trunk down\n"
            + "Interface                         IP Address/Mask      Physical   Protocol VPN \n"
            + "GigabitEthernet1/0/1              10.230.10.1/30       down       down     --  \n"
            + "\n";

    @Test
    public void testParseConfigAddress() {
        ConfigBuilder actual = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(actual, DISPLAY_IP_INT_BR_OUTPUT);
        Assert.assertEquals(AbstractIpv4ConfigReaderTest.buildData("10.230.10.1", "30"), actual.build());
    }
}

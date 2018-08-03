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

package io.frinx.cli.unit.brocade.ifc.handler;

import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "GigabitEthernet1/1 is up, line protocol is up \n"
            + "GigabitEthernet1/2 is up, line protocol is up \n"
            + "GigabitEthernet3/20 is down, line protocol is down \n"
            + "10GigabitEthernet4/1 is up, line protocol is up \n"
            + "10GigabitEthernet4/2 is up, line protocol is up \n"
            + "Ethernetmgmt1 is down, line protocol is down \n"
            + "Ve3 is down, line protocol is down \n"
            + "Ve210 is down, line protocol is down \n"
            + "Loopback1 is up, line protocol is up \n"
            + "Loopback2 is up, line protocol is up \n";

    @Test
    public void testAllIds() throws Exception {
        List<InterfaceKey> interfaceKeys = InterfaceReader.parseAllInterfaceIds(OUTPUT);
        Assert.assertThat(interfaceKeys, CoreMatchers.hasItem(new InterfaceKey("10GigabitEthernet4/2")));
    }
}
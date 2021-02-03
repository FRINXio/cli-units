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

package io.frinx.cli.unit.ios.qos.handler.ifc;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = " GigabitEthernet0/9 \n"
            + " GigabitEthernet0/8 \n"
            + " GigabitEthernet0/5 \n"
            + " GigabitEthernet0/5 \n";

    @Test
    public void testIds() {
        final List<InterfaceKey> allIds = InterfaceReader.getAllIds(OUTPUT);
        Assert.assertEquals(3, allIds.size());
        Assert.assertEquals("GigabitEthernet0/9", allIds.get(0).getInterfaceId());
        Assert.assertEquals("GigabitEthernet0/8", allIds.get(1).getInterfaceId());
        Assert.assertEquals("GigabitEthernet0/5", allIds.get(2).getInterfaceId());
    }

}
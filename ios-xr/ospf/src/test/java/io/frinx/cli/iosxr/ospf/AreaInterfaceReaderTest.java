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

package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;

public class AreaInterfaceReaderTest {

    private static final String OUTPUT = "Fri Feb 23 06:02:43.733 UTC\n" +
            " area 1\n" +
            "  interface Loopback4\n" +
            "  interface Loopback97\n" +
            "  interface GigabitEthernet0/0/0/3\n" +
            "  interface GigabitEthernet0/0/0/4\n" +
            " area 4\n" +
            "  interface GigabitEthernet0/0/0/2";

    @Test
    public void test() {

        Assert.assertArrayEquals(
                Lists.newArrayList("Loopback4", "Loopback97", "GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/4").toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT, "1").stream().map(InterfaceKey::getId).toArray());

        Assert.assertArrayEquals(
                Lists.newArrayList("GigabitEthernet0/0/0/2").toArray(),
                AreaInterfaceReader.parseInterfaceIds(OUTPUT, "4").stream().map(InterfaceKey::getId).toArray());
    }
}

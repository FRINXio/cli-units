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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.subifc;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;

public class L2VSISubInterfaceReaderTest {
    private static final String OUTPUT =
            "virtual-switch interface attach sub-port 210 vs IPVPN 1201\n"
                    + "virtual-switch interface attach sub-port 220 vs IPVPN 1201\n"
                    + "virtual-switch interface attach sub-port 230 vs IPVPN 1201\n"
                    + "virtual-switch interface attach sub-port 240 vs IPVPN 1201\n";

    @Test
    public void getAllIdsTest() {
        List<SubinterfaceKey> expected = Arrays.asList(
                new SubinterfaceKey(Long.valueOf("210")),
                new SubinterfaceKey(Long.valueOf("220")),
                new SubinterfaceKey(Long.valueOf("230")),
                new SubinterfaceKey(Long.valueOf("240")));

        Assert.assertEquals(expected, L2VSISubInterfaceReader.getAllIds(OUTPUT, "IPVPN 1201"));
    }
}

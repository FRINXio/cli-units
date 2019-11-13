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

package io.frinx.cli.unit.ios.mpls.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ldp.rev180702.mpls.ldp._interface.attributes.top._interface.attributes.interfaces.InterfaceKey;

public class LdpInterfaceReaderTest {

    private static final String OUTPUT = "Tunnel2                Yes           No       No  No     No\n"
            + "Tunnel100              Yes           No       No  No     No\n\n";

    private static final String INVALID_OUTPUT = "show mpls interfaces | exclude Interface\n"
            + "          ^\n"
            + "% Invalid input detected at '^' marker.\n";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = LdpInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("Tunnel2", "Tunnel100"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }

    @Test
    public void testInvalidIds() {
        List<InterfaceKey> keys = LdpInterfaceReader.getInterfaceKeys(INVALID_OUTPUT);
        Assert.assertTrue(keys.isEmpty());
    }

}

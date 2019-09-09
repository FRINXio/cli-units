/*
 * Copyright © 2019 Frinx and others.
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.rsvp.rev170824.rsvp.global.rsvp.te._interface.attributes.InterfaceKey;

public class RsvpInterfaceReaderTest {

    private static final String OUTPUT = "interface Tunnel1\n"
            + " ip rsvp bandwidth percent 50\n"
            + " ip rsvp tunnel overhead-percent 4\n"
            + "interface FastEthernet0/0\n"
            + "interface GigabitEthernet2/0\n"
            + "interface Tunnel2\n"
            + " ip rsvp bandwidth percent 50\n"
            + " ip rsvp tunnel overhead-percent 4\n"
            + "interface FastEthernet0/0\n"
            + "interface Tunnel3\n"
            + " ip rsvp bandwidth percent 30\n"
            + " ip rsvp tunnel overhead-percent 4\n"
            + "interface GigabitEthernet1/0\n"
            + "interface GigabitEthernet2/0\n"
            + "interface GigabitEthernet3/0\n"
            + "interface GigabitEthernet4/0\n"
            + "interface GigabitEthernet5/0\n"
            + "interface GigabitEthernet6/0\n"
            + "interface Tunnel4\n"
            + " ip rsvp bandwidth percent 35\n"
            + " ip rsvp tunnel overhead-percent 4\n";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = RsvpInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("Tunnel1", "Tunnel2","Tunnel3","Tunnel4"),
                keys.stream()
                        .map(InterfaceKey::getInterfaceId)
                        .map(InterfaceId::getValue)
                        .collect(Collectors.toList()));
    }

}

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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip4;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4AddressReaderTest {

    @Test
    public void testParse() throws Exception {
        List<AddressKey> addressKeys = Ipv4AddressReader.parseAddressIds(" ip address 192.168.1.44 255.255.255.0\n");
        ArrayList<AddressKey> expected = Lists.newArrayList(new AddressKey(new Ipv4AddressNoZone("192.168.1.44")));
        assertEquals(expected, addressKeys);
    }
}
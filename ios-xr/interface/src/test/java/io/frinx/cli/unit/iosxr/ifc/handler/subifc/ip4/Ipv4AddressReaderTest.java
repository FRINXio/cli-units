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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4AddressReaderTest {
    private static final String SH_RUN_INT_IP = "Mon Feb 12 12:47:42.025 UTC\n"+
            " ipv4 address 10.0.0.5 255.255.255.0\n";

    private static final String SH_RUN_INT_NO_IP = "Mon Feb 12 12:53:52.860 UTC";

    private static final List<AddressKey> EXPECTED_ADDRESSES = Lists.newArrayList("10.0.0.5")
            .stream().map(Ipv4AddressNoZone::new)
            .map(AddressKey::new)
            .collect(Collectors.toList());

    @Test
    public void testparseAddressConfig() {
        Assert.assertEquals(EXPECTED_ADDRESSES, Ipv4AddressReader.parseAddressIds(SH_RUN_INT_IP));
        Assert.assertTrue(Ipv4AddressReader.parseAddressIds(SH_RUN_INT_NO_IP).isEmpty());
    }
}

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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6AddressReaderTest {

    private static final String SH_RUN_INT_IPV6 = "Mon Feb 12 13:25:08.172 UTC\n"
            + " ipv6 address fe80::260:3eff:fe11:6770 link-local\n"
            + " ipv6 address 2001:db8:a0b:12f0::1/64";

    private static final List<AddressKey> EXPECTED_ADRESS_IDS =
            Lists.newArrayList("fe80::260:3eff:fe11:6770", "2001:db8:a0b:12f0::1")
            .stream()
            .map(Ipv6AddressNoZone::new)
            .map(AddressKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseAddressIds() {
        Assert.assertEquals(EXPECTED_ADRESS_IDS,
                new Ipv6AddressReader(Mockito.mock(Cli.class)).parseAddressIds(SH_RUN_INT_IPV6));
    }
}
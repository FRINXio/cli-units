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

package io.frinx.cli.unit.junos.ifc.handler.subifc.ip4;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4AddressReaderTest {

    private static final String OUTPUT_SINGLE = "set interfaces ge-0/0/3 unit 0 family inet address 10.11.12.13/16";

    private Ipv4AddressReader reader;

    @Before
    public void setUp() {
        reader = new Ipv4AddressReader(Mockito.mock(Cli.class));
    }

    @Test
    public void testParseAddressConfig() {
        final List<AddressKey> result = reader.parseAddressIds(OUTPUT_SINGLE);

        Assert.assertEquals(result.size(),1);
        Assert.assertEquals(Lists.newArrayList("10.11.12.13"),
            result.stream().map(AddressKey::getIp).map(Ipv4AddressNoZone::getValue).collect(Collectors.toList()));
    }
}

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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip6;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv6.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

class Ipv6VrrpGroupConfigReaderTest {

    private static final String OUTPUT = """
             vrrp 2 address-family ipv6
              track 100 shutdown
              address FE81::10 primary
              address 2001:DB8:85A3::7334/64
              address 2001:DB8:85A3::7335/64
            """;

    @Test
    void testParse() {
        ConfigBuilder expected = new ConfigBuilder()
            .setVirtualRouterId((short) 2)
            .setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv6Address("FE81::10"))))
            .addAugmentation(Config2.class, new Config2Builder()
                .setAddresses(Arrays.asList(
                    new Ipv6Prefix("2001:DB8:85A3::7334/64"),
                    new Ipv6Prefix("2001:DB8:85A3::7335/64")
                ))
                .setTrackedObjects(Collections.singletonList(
                    new TrackedObjectsBuilder()
                        .setTrackedObjectId(100)
                        .setShutdown(true)
                        .build()
                ))
            .build());

        ConfigBuilder builder = new ConfigBuilder();
        Ipv6VrrpGroupConfigReader.parseVrrpGroupConfig(OUTPUT, builder, (short) 2);
        assertEquals(expected.build(), builder.build());
    }
}

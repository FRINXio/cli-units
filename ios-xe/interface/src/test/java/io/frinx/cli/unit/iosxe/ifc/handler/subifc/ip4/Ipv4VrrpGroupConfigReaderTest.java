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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc.ip4;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv4.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class Ipv4VrrpGroupConfigReaderTest {

    private static final String OUTPUT = "vrrp 1 address-family ipv4\n"
            + "  priority 110\n"
            + "  preempt delay minimum 10\n"
            + "  track 900 decrement 11\n"
            + "  track 901 decrement 11\n"
            + "  track 902 decrement 11\n"
            + "  track 903 decrement 11\n"
            + "  address 192.168.100.0 primary\n"
            + "  address 192.168.101.0 secondary\n"
            + "  address 192.168.102.0 secondary\n"
            + "  exit-vrrp";

    @Test
    public void testParse() {
        ConfigBuilder expected = new ConfigBuilder()
            .setPriority((short) 110)
            .setPreemptDelay(10)
            .setVirtualRouterId((short) 1)
            .setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv4Address("192.168.100.0"))))
            .addAugmentation(Config1.class, new Config1Builder()
                .setVirtualSecondaryAddresses(Arrays.asList(
                    new IpAddress(new Ipv4Address("192.168.101.0")),
                    new IpAddress(new Ipv4Address("192.168.102.0"))
                ))
                .setTrackedObjects(Arrays.asList(
                   new TrackedObjectsBuilder()
                       .setTrackedObjectId(900)
                       .setPriorityDecrement((short) 11)
                       .build(),
                    new TrackedObjectsBuilder()
                        .setTrackedObjectId(901)
                        .setPriorityDecrement((short) 11)
                        .build(),
                    new TrackedObjectsBuilder()
                        .setTrackedObjectId(902)
                        .setPriorityDecrement((short) 11)
                        .build(),
                    new TrackedObjectsBuilder()
                            .setTrackedObjectId(903)
                            .setPriorityDecrement((short) 11)
                            .build()
                ))
            .build());

        ConfigBuilder builder = new ConfigBuilder();
        Ipv4VrrpGroupConfigReader.parseVrrpGroupConfig(OUTPUT, builder, (short) 1);
        Assert.assertEquals(expected.build(), builder.build());
    }
}

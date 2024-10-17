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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv4.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

class Ipv4VrrpGroupConfigWriterTest {

    private static final String WRITE_OUTPUT = """
            configure terminal
            interface 2
            vrrp 1 address-family ipv4
            priority 110
            preempt delay minimum 10
            track 900 decrement 11
            track 901 decrement 11
            track 902 decrement 11
            track 903 decrement 11
            track 100 shutdown
            no track 101 shutdown
            address 192.168.100.0 primary
            address 192.168.101.0 secondary
            address 192.168.102.0 secondary
            end
            """;

    private static final String WRITE_WITHOUT_VIRTUAL_ADDRESS_OUTPUT = """
            configure terminal
            interface 2
            vrrp 1 address-family ipv4
            no priority
            no preempt delay
            track 900 decrement 11
            track 901 decrement 11
            track 904 decrement 11
            track 905 decrement 11
            address 192.168.101.0 secondary
            address 192.168.103.0 secondary
            end
            """;

    private static final String WRITE_WITHOUT_AUG_OUTPUT = """
            configure terminal
            interface 2
            vrrp 1 address-family ipv4
            priority 110
            preempt delay minimum 10
            address 192.168.100.0 primary
            end
            """;

    private static final String UPDATE_OUTPUT = """
            configure terminal
            interface 2
            vrrp 1 address-family ipv4
            no priority
            no preempt delay
            no track 902 decrement 11
            no track 903 decrement 11
            no track 100 shutdown
            no track 101 shutdown
            track 900 decrement 11
            track 901 decrement 11
            track 904 decrement 11
            track 905 decrement 11
            no address 192.168.100.0 primary
            no address 192.168.102.0 secondary
            address 192.168.101.0 secondary
            address 192.168.103.0 secondary
            end
            """;

    @Mock
    private Cli cli;

    private Ipv4VrrpGroupConfigWriter writer;
    private Config config1;
    private Config config2;
    private Config config3;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new Ipv4VrrpGroupConfigWriter(cli);

        config1 = new ConfigBuilder()
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
                        .build(),
                    new TrackedObjectsBuilder()
                        .setTrackedObjectId(100)
                        .setShutdown(true)
                        .build(),
                    new TrackedObjectsBuilder()
                        .setTrackedObjectId(101)
                        .setShutdown(false)
                        .build()
                ))
                .build()).build();

        config2 = new ConfigBuilder()
                .setVirtualRouterId((short) 1)
                .addAugmentation(Config1.class, new Config1Builder()
                        .setVirtualSecondaryAddresses(Arrays.asList(
                                new IpAddress(new Ipv4Address("192.168.101.0")),
                                new IpAddress(new Ipv4Address("192.168.103.0"))
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
                                        .setTrackedObjectId(904)
                                        .setPriorityDecrement((short) 11)
                                        .build(),
                                new TrackedObjectsBuilder()
                                        .setTrackedObjectId(905)
                                        .setPriorityDecrement((short) 11)
                                        .build()
                        ))
                        .build()).build();

        config3 = new ConfigBuilder()
                .setPriority((short) 110)
                .setPreemptDelay(10)
                .setVirtualRouterId((short) 1)
                .setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv4Address("192.168.100.0"))))
                .build();
    }

    @Test
    void writerTest() {
        String writerOutput = writer.getWriteTemplate(null, config1, "2", (short) 1);
        assertEquals(WRITE_OUTPUT, writerOutput);
    }

    @Test
    void writerWithoutVirtualAddressTest() {
        String writerOutput = writer.getWriteTemplate(null, config2, "2", (short) 1);
        assertEquals(WRITE_WITHOUT_VIRTUAL_ADDRESS_OUTPUT, writerOutput);
    }

    @Test
    void writerWithoutAugTest() {
        String writerOutput = writer.getWriteTemplate(null, config3, "2", (short) 1);
        assertEquals(WRITE_WITHOUT_AUG_OUTPUT, writerOutput);
    }

    @Test
    void updateTest() {
        String writerOutput = writer.getWriteTemplate(config1, config2, "2", (short) 1);
        assertEquals(UPDATE_OUTPUT, writerOutput);
    }
}

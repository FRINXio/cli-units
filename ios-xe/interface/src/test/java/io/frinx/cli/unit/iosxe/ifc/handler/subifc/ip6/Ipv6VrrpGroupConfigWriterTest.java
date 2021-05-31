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

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.Config2Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.cisco.vrrp.ext.rev210521.ipv6.vrrp.group.config.TrackedObjectsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ip.vrrp.top.vrrp.vrrp.group.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

public class Ipv6VrrpGroupConfigWriterTest {

    private static final String WRITE_OUTPUT = "configure terminal\n"
            + "interface 2\n"
            + "vrrp 1 address-family ipv6\n"
            + "track 100 shutdown\n"
            + "no track 101 shutdown\n"
            + "address FE81::10 primary\n"
            + "address 2001:DB8:85A3::7334/64\n"
            + "address 2001:DB8:85A3::7335/64\n"
            + "end\n";

    private static final String UPDATE_OUTPUT = "configure terminal\n"
            + "interface 2\n"
            + "vrrp 1 address-family ipv6\n"
            + "no track 100 shutdown\n"
            + "no track 101 shutdown\n"
            + "track 200 shutdown\n"
            + "no track 201 shutdown\n"
            + "no address FE81::10 primary\n"
            + "no address 2001:DB8:85A3::7334/64\n"
            + "address 2001:DB8:85A3::7335/64\n"
            + "address 2001:DB8:85B3::7335/64\n"
            + "end\n";

    @Mock
    private Cli cli;

    private Ipv6VrrpGroupConfigWriter writer;
    private Config config1;
    private Config config2;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new Ipv6VrrpGroupConfigWriter(cli);

        config1 = new ConfigBuilder()
                .setVirtualRouterId((short) 1)
                .setVirtualAddress(Collections.singletonList(new IpAddress(new Ipv6Address("FE81::10"))))
                .addAugmentation(Config2.class, new Config2Builder()
                        .setAddresses(Arrays.asList(
                                new Ipv6Prefix("2001:DB8:85A3::7334/64"),
                                new Ipv6Prefix("2001:DB8:85A3::7335/64")
                        ))
                        .setTrackedObjects(Arrays.asList(
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
                .addAugmentation(Config2.class, new Config2Builder()
                        .setAddresses(Arrays.asList(
                                new Ipv6Prefix("2001:DB8:85A3::7335/64"),
                                new Ipv6Prefix("2001:DB8:85B3::7335/64")
                        ))
                        .setTrackedObjects(Arrays.asList(
                                new TrackedObjectsBuilder()
                                        .setTrackedObjectId(200)
                                        .setShutdown(true)
                                        .build(),
                                new TrackedObjectsBuilder()
                                        .setTrackedObjectId(201)
                                        .setShutdown(false)
                                        .build()
                        ))
                        .build()).build();
    }

    @Test
    public void writerTest() {
        String writerOutput = writer.getWriteTemplate(null, config1, "2", (short) 1);
        Assert.assertEquals(WRITE_OUTPUT, writerOutput);
    }

    @Test
    public void updateTest() {
        String writerOutput = writer.getWriteTemplate(config1, config2, "2", (short) 1);
        Assert.assertEquals(UPDATE_OUTPUT, writerOutput);
    }
}

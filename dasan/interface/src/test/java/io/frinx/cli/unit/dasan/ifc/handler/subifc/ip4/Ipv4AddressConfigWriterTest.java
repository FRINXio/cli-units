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

package io.frinx.cli.unit.dasan.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.IanaInterfaceType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4AddressConfigWriterTest {

    private static final String WRITE_INPUT = "configure terminal\n"
            + "interface br105\n"
            + "no ip address\n"
            + "end\n";
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private Cli cli;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @Mock
    private WriteContext context;
    private Ipv4AddressConfigWriter target;
    private InstanceIdentifier<Config> id;
    // test data
    private Config data;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new Ipv4AddressConfigWriter(cli));

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

    }

    private void prepare(Class<? extends IanaInterfaceType> ifType, String ifName) {
        final InterfaceKey interfaceKey = new InterfaceKey(ifName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(0L);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")))
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("10")).build();
    }

    private void prepare1(Class<? extends IanaInterfaceType> ifType, String ifName) {
        final InterfaceKey interfaceKey = new InterfaceKey(ifName);
        final SubinterfaceKey SubinterfaceKey = new SubinterfaceKey(1L);

        id = InstanceIdentifier.create(Interfaces.class).child(Interface.class, interfaceKey).child(Subinterfaces.class)
                .child(Subinterface.class, SubinterfaceKey).augmentation(Subinterface1.class).child(Ipv4.class)
                .child(Addresses.class).child(Address.class, new AddressKey(new Ipv4AddressNoZone("10.187.100.49")))
                .child(Config.class);

        ConfigBuilder builder = new ConfigBuilder();
        data = builder.setPrefixLength(Short.valueOf("10")).build();
    }

    @Test
    public void testWriteCurrentAttributes_001() throws Exception {
        prepare1(Ieee8023adLag.class, "Vlan100");

        thrown.expect(IllegalArgumentException.class);

        target.writeCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.never()).executeAndRead(Mockito.any());
    }

    @Test
    public void testDeleteCurrentAttributes_001() throws Exception {
        prepare(Ieee8023adLag.class, "Vlan105");

        target.deleteCurrentAttributes(id, data, context);

        Mockito.verify(cli, Mockito.atLeastOnce()).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }
}

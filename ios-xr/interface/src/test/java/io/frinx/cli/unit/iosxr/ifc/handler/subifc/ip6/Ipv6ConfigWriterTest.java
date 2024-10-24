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

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.Ipv6;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class Ipv6ConfigWriterTest {

    private static final String WRITE_INPUT = "interface GigabitEthernet 0/0/0/0\n"
            + "ipv6 address "
            +
            TestData.IPV6_ADDRESS_CONFIG.getIp()
                    .getValue() + "/" + TestData.IPV6_ADDRESS_CONFIG.getPrefixLength() + "\n"
            + "root\n";

    private static final String DELETE_INPUT = """
            interface GigabitEthernet 0/0/0/0
            no ipv6 address
            root
            """;

    private Cli cliMock;
    private WriteContext context;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    @BeforeEach
    void setUp() throws Exception {
        cliMock = Mockito.mock(Cli.class);
        context = Mockito.mock(WriteContext.class);

        Mockito.when(cliMock.executeAndRead(Mockito.any()))
                .then(invocation -> CompletableFuture.completedFuture(""));
    }

    @Test
    void settingIpv6Address_LAGInterface_goldenPathTest() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6ConfigWriter writer = new Ipv6ConfigWriter(cliMock);

        writer.writeCurrentAttributes(TestData.ADDRESS_CONFIG_IID_CORRECT, TestData.IPV6_ADDRESS_CONFIG, context);

        Mockito.verify(cliMock, Mockito.times(1))
                .executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue()
                .getContent());
    }

    @Test
    void settingIpv6Address_noLAGInterface() {
        Mockito.when(context.readAfter(Mockito.any()))
            .then(invocation -> Optional.of(TestData.INTERFACE_WRONG_TYPE));

        final Ipv6ConfigWriter writer = new Ipv6ConfigWriter(cliMock);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            writer.writeCurrentAttributes(TestData.ADDRESS_CONFIG_IID_VLAN, TestData.IPV6_ADDRESS_CONFIG, context)
        );

        assertThat(exception.getMessage(), allOf(
            containsString(Ipv6CheckUtil.CHECK_PARENT_INTERFACE_TYPE_MSG_PREFIX),
            containsString(EthernetCsmacd.class.getSimpleName()),
            containsString(Ieee8023adLag.class.getSimpleName())
        ));
    }

    @Test
    void settingIpv6Address_LAGInterface_missingIPAddress() {
        Mockito.when(context.readAfter(Mockito.any()))
            .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6ConfigWriter writer = new Ipv6ConfigWriter(cliMock);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            writer.writeCurrentAttributes(
                TestData.ADDRESS_CONFIG_IID_CORRECT,
                TestData.IPV6_ADDRESS_CONFIG_NO_ADDRESS,
                context)
        );

        assertThat(exception.getMessage(), containsString(Ipv6ConfigWriter.MISSING_IP_ADDRESS_MSG));
    }

    @Test
    void settingIpv6Address_LAGInterface_missingPrefix() {
        Mockito.when(context.readAfter(Mockito.any()))
            .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6ConfigWriter writer = new Ipv6ConfigWriter(cliMock);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            writer.writeCurrentAttributes(
                TestData.ADDRESS_CONFIG_IID_CORRECT,
                TestData.IPV6_ADDRESS_CONFIG_NO_PREFIX,
                context)
        );

        assertThat(exception.getMessage(), containsString(Ipv6ConfigWriter.MISSING_PREFIX_LENGTH_MSG));
    }

    @Test
    void deleteIpv6Address_LAGInterface() throws WriteFailedException {
        Mockito.when(context.readAfter(Mockito.any()))
                .then(invocation -> Optional.of(TestData.INTERFACE_CORRECT_TYPE));

        final Ipv6ConfigWriter writer = new Ipv6ConfigWriter(cliMock);

        writer.deleteCurrentAttributes(TestData.ADDRESS_CONFIG_IID_CORRECT, TestData.IPV6_ADDRESS_CONFIG, context);

        Mockito.verify(cliMock, Mockito.times(1))
                .executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue()
                .getContent());
    }

    private static final class TestData {

        static final String INTERFACE_NAME = "GigabitEthernet 0/0/0/0";
        static final String INTERFACE_NAME_WRONG = "vlan 1";
        static final long SUBINTERFACE_INDEX = 0L;
        private static final Ipv6AddressNoZone IPV6_ADDRESS = new Ipv6AddressNoZone("1::");

        static InstanceIdentifier<Interface> interfaceIid(final String interfaceName) {
            return IIDs.INTERFACES.child(Interface.class, new InterfaceKey(interfaceName));
        }

        static final InstanceIdentifier<Config> ADDRESS_CONFIG_IID_CORRECT =
                interfaceIid(INTERFACE_NAME).child(Subinterfaces.class)
                        .child(Subinterface.class, new SubinterfaceKey(SUBINTERFACE_INDEX))
                        .augmentation(Subinterface2.class)
                        .child(Ipv6.class)
                        .child(Addresses.class)
                        .child(Address.class, new AddressKey(IPV6_ADDRESS))
                        .child(Config.class);

        static final InstanceIdentifier<Config> ADDRESS_CONFIG_IID_VLAN =
                interfaceIid(INTERFACE_NAME_WRONG).child(Subinterfaces.class)
                        .child(Subinterface.class, new SubinterfaceKey(SUBINTERFACE_INDEX))
                        .augmentation(Subinterface2.class)
                        .child(Ipv6.class)
                        .child(Addresses.class)
                        .child(Address.class, new AddressKey(IPV6_ADDRESS))
                        .child(Config.class);

        static final Interface INTERFACE_CORRECT_TYPE = new InterfaceBuilder()
                .setName(INTERFACE_NAME)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(Ieee8023adLag.class)
                                .build())
                .build();
        static final Interface INTERFACE_WRONG_TYPE = new InterfaceBuilder()
                .setName(INTERFACE_NAME_WRONG)
                .setConfig(
                        new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                .rev161222.interfaces.top.interfaces._interface.ConfigBuilder()
                                .setType(L2vlan.class)
                                .build())
                .build();
        static final Config IPV6_ADDRESS_CONFIG = new ConfigBuilder()
                .setIp(IPV6_ADDRESS)
                .setPrefixLength((short) 128)
                .build();
        public static final Config IPV6_ADDRESS_CONFIG_NO_ADDRESS = new ConfigBuilder()
                .setPrefixLength((short) 128)
                .build();
        static final Config IPV6_ADDRESS_CONFIG_NO_PREFIX = new ConfigBuilder()
                .setIp(IPV6_ADDRESS)
                .build();
    }
}

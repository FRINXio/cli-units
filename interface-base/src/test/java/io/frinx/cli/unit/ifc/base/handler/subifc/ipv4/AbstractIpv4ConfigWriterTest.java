/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.ifc.base.handler.subifc.ipv4;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.Subinterface1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.Ipv4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.Addresses;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.Address;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.Subinterfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AbstractIpv4ConfigWriterTest {

    @Mock
    private WriteContext context;

    @Mock
    private AbstractIpv4ConfigWriter writer;

    @Mock
    private Config data;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        Mockito.doCallRealMethod().when(writer)
                .writeCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .updateCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .deleteCurrentAttributes(Mockito.any(InstanceIdentifier.class),
                        Mockito.any(Config.class), Mockito.eq(context));
        Mockito.doCallRealMethod().when(writer)
                .isSupportedInterface(Mockito.any(InstanceIdentifier.class));

    }

    @Test
    void testWriteCurrentAttributes0Sub() throws WriteFailedException {
        final InstanceIdentifier<Config> instanceIdentifier0 = configIID(0L);

        writer.writeCurrentAttributes(instanceIdentifier0, data, context);
        Mockito.verify(writer).blockingWriteAndRead(Mockito.any(), Mockito.eq(instanceIdentifier0),
                Mockito.any(), Mockito.any());
    }

    @Test
    void testWriteCurrentAttributes1Sub() throws WriteFailedException {
        assertThrows(WriteFailedException.CreateFailedException.class, () -> {
            final InstanceIdentifier<Config> instanceIdentifier1 = configIID(1L);

            writer.writeCurrentAttributes(instanceIdentifier1, data, context);
            Mockito.verify(writer, Mockito.never()).blockingWriteAndRead(Mockito.any(Cli.class),
                    Mockito.eq(instanceIdentifier1), Mockito.any(Config.class), Mockito.anyString());
        });
    }

    @Test
    void testDeleteCurrentAttributes0Sub() throws WriteFailedException {
        final InstanceIdentifier<Config> instanceIdentifier0 = configIID(0L);

        writer.deleteCurrentAttributes(instanceIdentifier0, data, context);
        Mockito.verify(writer).blockingDeleteAndRead(Mockito.any(), Mockito.eq(instanceIdentifier0),
                Mockito.any());
    }

    @Test
    void testDeleteCurrentAttributes1Sub() throws WriteFailedException {
        assertThrows(WriteFailedException.DeleteFailedException.class, () -> {
            final InstanceIdentifier<Config> instanceIdentifier1 = configIID(1L);

            writer.deleteCurrentAttributes(instanceIdentifier1, data, context);
            Mockito.verify(writer, Mockito.never()).blockingDeleteAndRead(Mockito.any(Cli.class),
                    Mockito.eq(instanceIdentifier1), Mockito.anyString());
        });
    }

    @Test
    void testUpdateCurrentAttributes() throws WriteFailedException {
        assertThrows(WriteFailedException.UpdateFailedException.class, () -> {
            final InstanceIdentifier<Config> instanceIdentifier1 = configIID(1L);

            writer.updateCurrentAttributes(instanceIdentifier1, data, data, context);
            Mockito.verify(writer, Mockito.never()).blockingWriteAndRead(Mockito.any(Cli.class),
                    Mockito.eq(instanceIdentifier1), Mockito.any(Config.class), Mockito.anyString());
        });
    }

    // helper methods

    public static InstanceIdentifier<Config> configIID(Long subIfcId) {
        return configIID("test", subIfcId);
    }

    public static InstanceIdentifier<Config> configIID(String ifcName, Long subIfcId) {
        return InstanceIdentifier.create(Interfaces.class)
                .child(Interface.class, new InterfaceKey(ifcName)).child(Subinterfaces.class)
                .child(Subinterface.class, new SubinterfaceKey(subIfcId)).augmentation(Subinterface1.class)
                .child(Ipv4.class).child(Addresses.class).child(Address.class, new AddressKey(
                        new Ipv4AddressNoZone("1.1.1.1"))).child(Config.class);
    }
}
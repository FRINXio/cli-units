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

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.ifc.base.handler.subifc.ip4.AbstractIpv4ConfigReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@ExtendWith(MockitoExtension.class)
public final class AbstractIpv4ConfigReaderTest {

    @Mock
    private AbstractIpv4ConfigReader reader;

    @Mock
    private ReadContext ctx;

    private ConfigBuilder builder = new ConfigBuilder();

    @BeforeEach
    void setUp() throws Exception {
        Mockito.doCallRealMethod().when(reader)
                .readCurrentAttributes(Mockito.any(InstanceIdentifier.class), Mockito.eq(builder), Mockito.eq(ctx));
        Mockito.doCallRealMethod().when(reader).isSupportedInterface(Mockito.any(InstanceIdentifier.class));
    }

    @Test
    void testReadCurrentAttributes0Sub() throws ReadFailedException {
        final InstanceIdentifier<Config> instanceIdentifier0 = AbstractIpv4ConfigWriterTest.configIID(0L);

        reader.readCurrentAttributes(instanceIdentifier0, builder, ctx);
        Mockito.verify(reader).parseAddressConfig(Mockito.eq(builder), Mockito.any(),
                Mockito.any(Ipv4AddressNoZone.class));
    }

    @Test
    void testReadCurrentAttributes1Sub() throws ReadFailedException {
        final InstanceIdentifier<Config> instanceIdentifier1 = AbstractIpv4ConfigWriterTest.configIID(1L);

        reader.readCurrentAttributes(instanceIdentifier1, builder, ctx);
        Mockito.verify(reader, Mockito.never()).parseAddressConfig(Mockito.eq(builder), Mockito.anyString(),
                Mockito.any(Ipv4AddressNoZone.class));
    }

    // helper methods

    public static Config buildData(String ipAddress, String prefix) {
        return new ConfigBuilder()
                .setIp(new Ipv4AddressNoZone(ipAddress))
                .setPrefixLength(Short.valueOf(prefix))
                .build();
    }
}
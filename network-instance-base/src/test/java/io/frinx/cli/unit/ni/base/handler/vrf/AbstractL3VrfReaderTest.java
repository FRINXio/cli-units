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

package io.frinx.cli.unit.ni.base.handler.vrf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class AbstractL3VrfReaderTest {

    @Mock
    private AbstractL3VrfReader reader;

    @Mock
    private ReadContext ctx;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.doCallRealMethod().when(reader).readCurrentAttributes(Mockito.any(), Mockito.any(), Mockito.eq(ctx));
    }

    @Test
    void testReadCurrentAttributes() {
        final String vrfName = "VRF-001";
        final InstanceIdentifier<NetworkInstance> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrfName));

        final NetworkInstanceBuilder builder = new NetworkInstanceBuilder();

        reader.readCurrentAttributes(iid, builder, ctx);

        assertEquals(builder.getName(), vrfName);
    }
}

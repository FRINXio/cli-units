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

package io.frinx.cli.unit.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfV3ProtocolReaderTest {

    private static final String SH_RUN_OSPFV3 = "show running-config router ospfv3 | include ^router ospfv3";
    private static final String RESULT_SH_RUN_OSPFV3 = "router ospfv3 1000";

    @Mock
    private Cli cli;

    private final ProtocolKey protocolKey = new ProtocolKey(OSPF3.class, "1000");
    private final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
    private InstanceIdentifier<Protocol> iid = InstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, networkInstanceKey)
            .child(Protocols.class).child(Protocol.class, protocolKey);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadCurrentAttributesForType_001() {
        final ProtocolBuilder builder = new ProtocolBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        OspfV3ProtocolReader target = new OspfV3ProtocolReader(cli);
        // test
        target.readCurrentAttributes(iid, builder, ctx);
        // verify
        Assert.assertEquals(builder.build().getName(), protocolKey.getName());
        Assert.assertEquals(builder.build().getIdentifier(), protocolKey.getIdentifier());
    }

    @Test
    public void testGetAllIds_001() throws Exception {
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        OspfV3ProtocolReader target = Mockito.spy(new OspfV3ProtocolReader(cli));
        Mockito.doReturn(RESULT_SH_RUN_OSPFV3).when(target).blockingRead(SH_RUN_OSPFV3, cli, iid, ctx);
        // test
        List<ProtocolKey> result = target.getAllIds(iid, ctx);
        // verify
        Assert.assertThat(result.size(), CoreMatchers.is(1));
        Assert.assertEquals(result.get(0).getName(), "1000");
        Assert.assertEquals(result.get(0).getIdentifier().getName(),
                "org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3");
    }
}

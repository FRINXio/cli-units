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

package io.frinx.cli.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ProtocolOspfv3ExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.StubRouter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.top.Ospfv3;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.types.rev180817.STUBROUTERMAXMETRIC;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StubRouterConfigReaderTest {

    private static final String SH_OSPF_V3_STUBRTR =
            "show running-config router ospfv3 1000  stub-router router-lsa max-metric";
    private static final String RESULT_SH_RUN_OSPFV3 = "router ospfv3 1000\n stub-router router-lsa max-metric\n"
            + "  always\n !\n!";
    @Mock
    private Cli cli;

    private final ProtocolKey protocolKey = new ProtocolKey(OSPF3.class, "1000");
    private final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
    private InstanceIdentifier<Config> iid = InstanceIdentifier.create(NetworkInstances.class)
            .child(NetworkInstance.class, networkInstanceKey).child(Protocols.class)
            .child(Protocol.class, protocolKey).augmentation(ProtocolOspfv3ExtAug.class)
            .child(Ospfv3.class).child(Global.class)
            .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3
                    .rev180817.ospfv3.global.structural.global.Config.class)
            .child(StubRouter.class)
            .child(Config.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReadCurrentAttributesForType() throws ReadFailedException {
        final ConfigBuilder builder = new ConfigBuilder();
        final ReadContext ctx = Mockito.mock(ReadContext.class);
        StubRouterConfigReader target = Mockito.spy(new StubRouterConfigReader(cli));
        // test
        Mockito.doReturn(RESULT_SH_RUN_OSPFV3).when(target).blockingRead(SH_OSPF_V3_STUBRTR, cli, iid, ctx);
        target.readCurrentAttributes(iid, builder, ctx);
        // verify
        Assert.assertEquals(builder.build().getAdvertiseLsasTypes(), STUBROUTERMAXMETRIC.class);
        Assert.assertTrue(builder.build().isSet());
        Assert.assertTrue(builder.build().isAlways());
    }
}

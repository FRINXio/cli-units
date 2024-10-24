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

package io.frinx.cli.unit.iosxr.isis.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class IsisProtocolReaderTest {
    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis | include ^router isis";
    private static final String SH_RUN_ROUTER_ISIS_LINES = "router isis 1000";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private IsisProtocolReader target;

    private static final String INSTANCE_NAME = "1000";
    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final InstanceIdentifier<Protocol> IID_FOR_LIST = IidUtils.createIid(IIDs.NE_NE_PROTOCOLS,
        NetworInstance.DEFAULT_NETWORK)
        .child(Protocol.class);
    private static final InstanceIdentifier<Protocol> IID = IidUtils.createIid(IIDs.NE_NE_PR_PROTOCOL,
        NetworInstance.DEFAULT_NETWORK,
        PROTOCOL_KEY);

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new IsisProtocolReader(cli));
    }

    @Test
    void testGetAllIds_001() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_ROUTER_ISIS_LINES).when(target)
            .blockingRead(SH_RUN_ROUTER_ISIS, cli, IID_FOR_LIST, ctx);

        List<ProtocolKey> result = target.getAllIds(IID_FOR_LIST, ctx);

        assertThat(result.size(), CoreMatchers.is(1));
        assertThat(result.get(0).getName(), CoreMatchers.equalTo(INSTANCE_NAME));
        assertThat(result.get(0).getIdentifier(), CoreMatchers.equalTo(ISIS.class));
    }

    @Test
    void testReadCurrentAttributes() {
        final ProtocolBuilder builder = new ProtocolBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        assertThat(builder.getName(), CoreMatchers.equalTo(INSTANCE_NAME));
        assertThat(builder.getIdentifier(), CoreMatchers.equalTo(ISIS.class));
    }
}

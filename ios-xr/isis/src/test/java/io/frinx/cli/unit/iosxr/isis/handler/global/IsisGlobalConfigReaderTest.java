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

package io.frinx.cli.unit.iosxr.isis.handler.global;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisInternalLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.global.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisGlobalConfigReaderTest {
    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis 1000";
    private static final String SH_RUN_ROUTER_ISIS_LINES = "router isis 1000\n"
        + "  max-link-metric level 1\n"
        + "  max-link-metric level 2\n"
        + "  max-link-metric level 3\n"  // invalid level
        + "  max-link-metric unknown options\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private IsisGlobalConfigReader target;

    private static final String INSTANCE_NAME = "1000";
    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(IIDs.NE_NE_PR_PR_IS_GL_CONFIG,
        NetworInstance.DEFAULT_NETWORK,
        PROTOCOL_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new IsisGlobalConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_ROUTER_ISIS_LINES).when(target)
            .blockingRead(SH_RUN_ROUTER_ISIS, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);
        IsisGlobalConfAug augmentation = builder.getAugmentation(IsisGlobalConfAug.class);

        Assert.assertThat(augmentation.getMaxLinkMetric(),
            Matchers.containsInAnyOrder(IsisInternalLevel.LEVEL1, IsisInternalLevel.LEVEL2));
    }
}

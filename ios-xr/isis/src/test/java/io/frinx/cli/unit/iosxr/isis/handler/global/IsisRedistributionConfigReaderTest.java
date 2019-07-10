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
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.RedistributionKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.isis.redistribution.ext.config.redistributions.redistribution.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.IPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.LevelType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.types.rev181121.UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.afi.safi.list.AfKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class IsisRedistributionConfigReaderTest {
    private static final String INSTANCE_NAME = "ISIS-001";
    private static final String REDIST_INSTANCE = "ISIS-002";

    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis ISIS-001"
        + " address-family ipv4 unicast redistribute isis ISIS-002";
    private static final String SH_RUN_ROUTER_ISIS_LINES = "router isis ISIS-001\n"
        + " address-family ipv4 unicast\n"
        + "  redistribute isis ISIS-002 level-1 metric 10 route-policy POLICY-001 metric-type internal\n"
        + " !\n"
        + "!\n";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext ctx;
    private IsisRedistributionConfigReader target;

    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final AfKey AF_KEY = new AfKey(IPV4.class, UNICAST.class);
    private static final RedistributionKey REDIST_KEY = new RedistributionKey(REDIST_INSTANCE, "isis");
    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(
        IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_RE_CONFIG,
        NetworInstance.DEFAULT_NETWORK, PROTOCOL_KEY, AF_KEY, REDIST_KEY);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new IsisRedistributionConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributes() throws ReadFailedException {
        Mockito.doReturn(SH_RUN_ROUTER_ISIS_LINES).when(target)
            .blockingRead(SH_RUN_ROUTER_ISIS, cli, IID, ctx);

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(IID, builder, ctx);

        Assert.assertThat(builder.getLevel(), CoreMatchers.equalTo(LevelType.LEVEL1));
        Assert.assertThat(builder.getMetric(), CoreMatchers.equalTo(10L));
        Assert.assertThat(builder.getRoutePolicy(), CoreMatchers.equalTo("POLICY-001"));
    }
}

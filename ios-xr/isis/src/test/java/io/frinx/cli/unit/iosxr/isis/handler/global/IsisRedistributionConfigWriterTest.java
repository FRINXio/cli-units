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

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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

public class IsisRedistributionConfigWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private IsisRedistributionConfigWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String INSTANCE_NAME = "1000";
    private static final String REDIST_INST = "2000";
    private static final String REDIST_PROTO = "isis";

    private static final String WRITE_INPUT = "router isis 1000\n"
        + " address-family ipv4 unicast\n"
        + "  redistribute isis 2000 level-1 metric 100 route-policy ROUTE-POLICY-001\n"
        + "root\n";
    private static final String DELETE_INPUT = "router isis 1000\n"
        + " address-family ipv4 unicast\n"
        + "  no redistribute isis 2000\n"
        + "root\n";

    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final AfKey AF_KEY = new AfKey(IPV4.class, UNICAST.class);
    private static final RedistributionKey REDISTRIBUTION_KEY = new RedistributionKey(REDIST_INST, REDIST_PROTO);

    private static final InstanceIdentifier<Config> IID = IidUtils.createIid(
        IIDs.NE_NE_PR_PR_IS_GL_AF_AF_AUG_ISISGLOBALAFISAFICONFAUG_RE_RE_CONFIG,
        NetworInstance.DEFAULT_NETWORK,
        PROTOCOL_KEY,
        AF_KEY,
        REDISTRIBUTION_KEY);

    private static final Config DATA = new ConfigBuilder()
        .setProtocol("isis")
        .setInstance("2000")
        .setLevel(LevelType.LEVEL1)
        .setMetric(Long.valueOf(100L))
        .setRoutePolicy("ROUTE-POLICY-001")
        .build();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new IsisRedistributionConfigWriter(cli);
    }

    @Test
    public void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    public void testDeleteCurrentAttributes() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}

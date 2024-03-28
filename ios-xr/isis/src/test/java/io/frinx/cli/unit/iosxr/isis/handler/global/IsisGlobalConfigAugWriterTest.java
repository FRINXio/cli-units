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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisGlobalConfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.isis.extension.rev190311.IsisInternalLevel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class IsisGlobalConfigAugWriterTest {
    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;
    private IsisGlobalConfigAugWriter target;
    private ArgumentCaptor<Command> response = ArgumentCaptor.forClass(Command.class);

    private static final String INSTANCE_NAME = "1000";
    private static final String WRITE_INPUT = "router isis " + INSTANCE_NAME + "\n"
        + "max-link-metric \n"
        + "max-link-metric level 1\n"
        + "root\n";
    private static final String UPDATE_INPUT = "router isis " + INSTANCE_NAME + "\n"
        + "no max-link-metric level 1\n"
        + "max-link-metric level 2\n"
        + "root\n";
    private static final String DELETE_INPUT = "router isis " + INSTANCE_NAME + "\n"
        + "no max-link-metric \n"
        + "no max-link-metric level 2\n"
        + "root\n";

    private static final ProtocolKey PROTOCOL_KEY = new ProtocolKey(ISIS.class, INSTANCE_NAME);
    private static final InstanceIdentifier<IsisGlobalConfAug> IID = IidUtils.createIid(
        IIDs.NE_NE_PR_PR_IS_GL_CO_AUG_ISISGLOBALCONFAUG,
        NetworInstance.DEFAULT_NETWORK,
        PROTOCOL_KEY);

    private static final IsisGlobalConfAug DATA_BEFORE = new IsisGlobalConfAugBuilder()
        .setMaxLinkMetric(Lists.newArrayList(IsisInternalLevel.NOTSET, IsisInternalLevel.LEVEL1))
        .build();

    private static final IsisGlobalConfAug DATA_AFTER = new IsisGlobalConfAugBuilder()
        .setMaxLinkMetric(Lists.newArrayList(IsisInternalLevel.NOTSET, IsisInternalLevel.LEVEL2))
        .build();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        target = new IsisGlobalConfigAugWriter(cli);
    }

    @Test
    void testWriteCurrentAttributes() throws WriteFailedException {
        target.writeCurrentAttributes(IID, DATA_BEFORE, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(WRITE_INPUT, response.getValue().getContent());
    }

    @Test
    void testUpdateCurrentAttributes() throws WriteFailedException {
        target.updateCurrentAttributes(IID, DATA_BEFORE, DATA_AFTER, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(UPDATE_INPUT, response.getValue().getContent());
    }

    @Test
    void testDeleteCurrentAttributes() throws WriteFailedException {
        target.deleteCurrentAttributes(IID, DATA_AFTER, writeContext);
        Mockito.verify(cli).executeAndRead(response.capture());
        assertEquals(DELETE_INPUT, response.getValue().getContent());
    }
}

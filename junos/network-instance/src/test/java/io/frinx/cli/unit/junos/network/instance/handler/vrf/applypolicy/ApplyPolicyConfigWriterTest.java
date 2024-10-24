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

package io.frinx.cli.unit.junos.network.instance.handler.vrf.applypolicy;

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.InterInstancePolicies;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class ApplyPolicyConfigWriterTest {
    private static final String VRF_NAME = "VRF-001";
    private static final InstanceIdentifier<Config> IIDS_CONFIG = IIDs.NETWORKINSTANCES
        .child(NetworkInstance.class, new NetworkInstanceKey(VRF_NAME))
        .child(InterInstancePolicies.class)
        .child(ApplyPolicy.class)
        .child(Config.class);
    private static final List<String> IMPORT_POLICY_AFTER =
        Lists.newArrayList("IMPORT-POLICY-999", "IMPORT-POLICY-001");
    private static final Config CONFIG_AFTER = new ConfigBuilder()
        .setImportPolicy(IMPORT_POLICY_AFTER)
        .build();
    private static final List<String> IMPORT_POLICY_BEFORE =
        Lists.newArrayList("IMPORT-POLICY-888", "IMPORT-POLICY-002");
    private static final Config CONFIG_BEFORE = new ConfigBuilder()
        .setImportPolicy(IMPORT_POLICY_BEFORE)
        .build();

    @Mock
    private Cli cli;
    @Mock
    private WriteContext writeContext;

    private ApplyPolicyConfigWriter target;

    private ArgumentCaptor<Command> commands;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = new ApplyPolicyConfigWriter(cli);

        Mockito.doReturn(CompletableFuture.completedFuture("")).when(cli).executeAndRead(Mockito.any());
        commands = ArgumentCaptor.forClass(Command.class);
    }

    @Test
    void testWriteCurrentAttributes() throws Exception {
        target.writeCurrentAttributes(IIDS_CONFIG, CONFIG_AFTER, writeContext);

        Mockito.verify(cli, Mockito.times(2)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(2));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-999\n"));
        assertThat(commands.getAllValues().get(1).getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-001\n"));
    }

    @Test
    void testUpdateCurrentAttributes() throws Exception {
        target.updateCurrentAttributes(IIDS_CONFIG, CONFIG_BEFORE, CONFIG_AFTER, writeContext);

        Mockito.verify(cli, Mockito.times(4)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(4));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-888\n"));
        assertThat(commands.getAllValues().get(1).getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-002\n"));
        assertThat(commands.getAllValues().get(2).getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-999\n"));
        assertThat(commands.getAllValues().get(3).getContent(), CoreMatchers.equalTo(
            "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-001\n"));
    }

    @Test
    void testDeleteCurrentAttributes() throws Exception {
        target.deleteCurrentAttributes(IIDS_CONFIG, CONFIG_BEFORE, writeContext);

        Mockito.verify(cli, Mockito.times(2)).executeAndRead(commands.capture());

        assertThat(commands.getAllValues().size(), CoreMatchers.is(2));
        assertThat(commands.getAllValues().get(0).getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-888\n"));
        assertThat(commands.getAllValues().get(1).getContent(), CoreMatchers.equalTo(
            "delete routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-002\n"));
    }
}

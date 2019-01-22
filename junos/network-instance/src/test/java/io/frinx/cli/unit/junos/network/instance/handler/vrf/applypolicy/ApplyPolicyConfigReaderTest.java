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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class ApplyPolicyConfigReaderTest {
    private static final String VRF_NAME = "VRF-001";
    private static final InstanceIdentifier<Config> IIDS_CONFIG = IIDs.NETWORKINSTANCES
        .child(NetworkInstance.class, new NetworkInstanceKey(VRF_NAME))
        .child(InterInstancePolicies.class)
        .child(ApplyPolicy.class)
        .child(Config.class);
    private static final String SH_CONFIG = "show configuration routing-instances VRF-001 "
        + "routing-options instance-import | display set";
    private static final String SH_CONFIG_OUTPUT =
        "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-999\n"
        + "set routing-instances VRF-001 routing-options instance-import IMPORT-POLICY-001\n"
        + "set routing-instances VRF-001 routing-options other-options EXPRT-POLICY-002";
    private static final List<String> EXPECTED_IMPORT_POLICY =
        Lists.newArrayList("IMPORT-POLICY-999", "IMPORT-POLICY-001");

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private ApplyPolicyConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new ApplyPolicyConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributesForType() throws Exception {
        ConfigBuilder configBuilder = new ConfigBuilder();

        Mockito.doReturn(SH_CONFIG_OUTPUT).when(target)
            .blockingRead(Mockito.eq(SH_CONFIG), Mockito.eq(cli), Mockito.eq(IIDS_CONFIG), Mockito.eq(readContext));

        target.readCurrentAttributesForType(IIDS_CONFIG, configBuilder , readContext);

        Assert.assertThat(configBuilder.getImportPolicy(), CoreMatchers.equalTo(EXPECTED_IMPORT_POLICY));
    }
}

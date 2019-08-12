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

package io.frinx.cli.unit.brocade.network.instance.vrf.policy.forwarding;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;

public class VrfPFInterfaceConfigWriterTest {

    @Test
    public void writeOutput() {
        Config config = new ConfigBuilder().setInterfaceId(new InterfaceId("eth 1/3")).build();
        NiPfIfBrocadeAug brcd = new NiPfIfBrocadeAugBuilder()
                .setInputServicePolicy("policy_test")
                .setOutputServicePolicy("policy_test").build();

        VrfPFInterfaceConfigWriter writer =
                new VrfPFInterfaceConfigWriter(Mockito.mock(Cli.class));
        Assert.assertEquals("configure terminal\n"
                + "interface eth 1/3\n"
                + "rate-limit input policy-map policy_test\n"
                + "exit\n"
                + "interface eth 1/3\n"
                + "rate-limit output policy-map policy_test\n"
                + "end", writer.getCommand(config, brcd, null, false));

        Assert.assertEquals("configure terminal\n"
                        + "interface eth 1/3\n"
                        + "no rate-limit input policy-map policy_test\n"
                        + "exit\n"
                        + "interface eth 1/3\n"
                        + "no rate-limit output policy-map policy_test\n"
                        + "end",
                writer.getCommand(config, null, brcd, true));
    }
}
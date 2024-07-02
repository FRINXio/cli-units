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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.pf.interfaces.extension.brocade.rev190726.NiPfIfBrocadeAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.forwarding.rev170621.pf.interfaces.structural.interfaces._interface.ConfigBuilder;

class VrfPFInterfaceConfigWriterTest {

    @Test
    void writeOutput() {
        Config config = new ConfigBuilder().setInterfaceId(new InterfaceId("eth 1/3")).build();
        NiPfIfBrocadeAug brcd = new NiPfIfBrocadeAugBuilder()
                .setInputServicePolicy("policy_test")
                .setOutputServicePolicy("policy_test").build();

        VrfPFInterfaceConfigWriter writer =
                new VrfPFInterfaceConfigWriter(Mockito.mock(Cli.class));
        assertEquals("""
                configure terminal
                interface eth 1/3
                rate-limit input policy-map policy_test
                exit
                interface eth 1/3
                rate-limit output policy-map policy_test
                end""", writer.getCommand(config, brcd, null, false));

        assertEquals("""
                        configure terminal
                        interface eth 1/3
                        no rate-limit input policy-map policy_test
                        exit
                        interface eth 1/3
                        no rate-limit output policy-map policy_test
                        end""",
                writer.getCommand(config, null, brcd, true));
    }
}
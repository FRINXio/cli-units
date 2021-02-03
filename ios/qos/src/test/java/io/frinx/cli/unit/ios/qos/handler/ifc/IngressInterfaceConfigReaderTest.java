/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.qos.handler.ifc;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIngressInterfaceAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos._interface.input.top.input.ConfigBuilder;

public class IngressInterfaceConfigReaderTest {

    private static final String OUTPUT = "Building configuration...\n"
            + "\n"
            + "Current configuration : 99 bytes\n"
            + "!\n"
            + "interface GigabitEthernet0/1\n"
            + " service-policy input TESTINPUT\n"
            + " service-policy output TESTOUTPUT\n"
            + "end\n"
            + "\n";

    @Test
    public void test() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        IngressInterfaceConfigReader.fillInConfig(OUTPUT, configBuilder);
        final QosIngressInterfaceAug aug = configBuilder.getAugmentation(QosIngressInterfaceAug.class);
        Assert.assertEquals("TESTINPUT", aug.getServicePolicy());
    }

}
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

package io.frinx.cli.unit.iosxe.ifc.handler.service.instance;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.Encapsulation;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.service.instance.EncapsulationBuilder;

public class ServiceInstanceEncapsulationReaderTest {

    private static final Encapsulation CLEAN_ENCAPSULATION = new EncapsulationBuilder()
            .build();

    private static final String CLEAN_OUTPUT = " service instance 1 ethernet\n";

    private static final Encapsulation UNTAGGED_ENCAPSULATION = new EncapsulationBuilder()
            .setUntagged(true)
            .build();

    private static final String UNTAGGED_OUTPUT = " service instance 1 ethernet\n"
            + "  encapsulation untagged\n";

    private static final Encapsulation DOT1Q_ENCAPSULATION = new EncapsulationBuilder()
            .setUntagged(false)
            .setDot1q(Arrays.asList("1-3", "5-10", "11"))
            .build();

    private static final String DOT1Q_OUTPUT = " service instance 1 ethernet\n"
            + "  encapsulation dot1q 1-3, 5-10, 11\n";

    private static final Encapsulation BOTH_ENCAPSULATIONS = new EncapsulationBuilder()
            .setUntagged(true)
            .setDot1q(Arrays.asList("1-3", "5-10"))
            .build();

    private static final String BOTH_OUTPUT = " service instance 1 ethernet\n"
            + "  encapsulation untagged , dot1q 1-3,5-10\n";

    private final EncapsulationBuilder encapsulationBuilder = new EncapsulationBuilder();

    @Test
    public void testClean() {
        ServiceInstanceEncapsulationReader.parseEncapsulation(CLEAN_OUTPUT, encapsulationBuilder);
        Assert.assertEquals(CLEAN_ENCAPSULATION, encapsulationBuilder.build());
    }

    @Test
    public void testUntagged() {
        ServiceInstanceEncapsulationReader.parseEncapsulation(UNTAGGED_OUTPUT, encapsulationBuilder);
        Assert.assertEquals(UNTAGGED_ENCAPSULATION, encapsulationBuilder.build());
    }

    @Test
    public void testDot1q() {
        ServiceInstanceEncapsulationReader.parseEncapsulation(DOT1Q_OUTPUT, encapsulationBuilder);
        Assert.assertEquals(DOT1Q_ENCAPSULATION, encapsulationBuilder.build());
    }

    @Test
    public void testBoth() {
        ServiceInstanceEncapsulationReader.parseEncapsulation(BOTH_OUTPUT, encapsulationBuilder);
        Assert.assertEquals(BOTH_ENCAPSULATIONS, encapsulationBuilder.build());
    }

}
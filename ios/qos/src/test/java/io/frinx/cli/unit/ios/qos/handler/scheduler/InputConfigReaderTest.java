/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.ios.qos.handler.scheduler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.Cos;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosCosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.inputs.top.inputs.input.ConfigBuilder;

public class InputConfigReaderTest {

    private static final String OUTPUT = "  Policy Map plmap\r\n"
            + "    Class class-default\r\n"
            + "      set cos 5\r\n"
            + "    Class map\r\n"
            + "      priority\r\n";

    private ConfigBuilder configBuilder;

    @Before
    public void setup() {
        this.configBuilder = new ConfigBuilder();
    }

    @Test
    public void testCos() {
        InputConfigReader.parseConfig("class-default", OUTPUT, configBuilder);
        final QosCosAug qosCosAug = configBuilder.getAugmentation(QosCosAug.class);
        Assert.assertEquals(Cos.getDefaultInstance("5"), qosCosAug.getCos());
    }

    @Test
    public void testNullCos() {
        InputConfigReader.parseConfig("map", OUTPUT, configBuilder);
        Assert.assertNull(configBuilder.getAugmentation(QosCosAug.class));
    }

}
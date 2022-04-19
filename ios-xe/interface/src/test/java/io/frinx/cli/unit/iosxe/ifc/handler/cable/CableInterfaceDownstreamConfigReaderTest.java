/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.cable;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.downstream.top.downstream.ConfigBuilder;

public class CableInterfaceDownstreamConfigReaderTest {

    static final String OUTPUT = " downstream Integrated-Cable 1/0/0 rf-channel 0\n"
            + " downstream Integrated-Cable 1/0/0 rf-channel 2\n"
            + " downstream Integrated-Cable 1/0/0 rf-channel 8\n"
            + " downstream Integrated-Cable 1/0/0 rf-channel 10\n";

    @Test
    public void test() {
        final ConfigBuilder builder = new ConfigBuilder();
        CableInterfaceDownstreamConfigReader.parseConfig(OUTPUT, builder);
        Assert.assertEquals("Integrated-Cable1/0/0", builder.getName());
        Assert.assertEquals("0 2 8 10", builder.getRfChannels());
    }
}

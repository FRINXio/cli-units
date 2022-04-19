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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rphy.extension.rev220214.upstream.top.upstream.upstream.cables.ConfigBuilder;

public class CableInterfaceUpstreamConfigReaderTest {

    @Test
    public void test() {
        final ConfigBuilder builder = new ConfigBuilder();
        CableInterfaceUpstreamConfigReader.parseConfig(CableInterfaceUpstreamReaderTest.OUTPUT, "1", builder);
        Assert.assertEquals("Upstream-Cable1/0/11", builder.getName());
        Assert.assertEquals("1", builder.getUsChannel());
    }
}

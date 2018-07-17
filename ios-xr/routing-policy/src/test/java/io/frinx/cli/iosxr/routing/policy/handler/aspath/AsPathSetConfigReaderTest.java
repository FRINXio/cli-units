/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.routing.policy.handler.aspath;

import static io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetConfigWriterTest.CFG_1;
import static io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetConfigWriterTest.CFG_2;
import static io.frinx.cli.iosxr.routing.policy.handler.aspath.AsPathSetConfigWriterTest.OUTPUT_2;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.AsPathSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.as.path.set.top.as.path.sets.as.path.set.ConfigBuilder;

public class AsPathSetConfigReaderTest {

    static final String OUTPUT_1 = "as-path-set test\r\n"
            + "  neighbor-is '1.1',\r\n"
            + "  passes-through '54',\n"
            + "  originates-from '3243',\n"
            + "  length eq 444,\r\n"
            + "  unique-length eq 44,\n"
            + "  ios-regex '*'\n"
            + "end-set\n"
            + "";

    @Test
    public void testRead() throws Exception {
        ConfigBuilder builder = new ConfigBuilder();
        AsPathSetConfigReader.parseMembers(builder, new AsPathSetKey("test"), OUTPUT_1);
        assertEquals(CFG_1, builder.build());

        builder = new ConfigBuilder();
        AsPathSetConfigReader.parseMembers(builder, new AsPathSetKey("test"), OUTPUT_2);
        assertEquals(CFG_2, builder.build());
    }
}
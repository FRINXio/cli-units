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

package io.frinx.cli.iosxr.ospf;

import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.igp.ldp.sync.ConfigBuilder;

public class AreaInterfaceMplsSyncConfigReaderTest {

    private final String outputDisable = "  interface GigabitEthernet0/0/0/3\n"
            + "   cost 100\n"
            + "   mpls ldp sync disable\n";

    private final String outputEnable = "  interface GigabitEthernet0/0/0/3\n"
            + "   cost 100\n"
            + "   mpls ldp sync\n";

    private final String outputNotSet = "  interface GigabitEthernet0/0/0/3\n"
            + "   cost 100\n";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputDisable, builder);
        Assert.assertFalse(builder.isEnabled());

        builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputEnable, builder);
        Assert.assertTrue(builder.isEnabled());

        builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputNotSet, builder);
        Assert.assertNull(builder.isEnabled());
    }
}

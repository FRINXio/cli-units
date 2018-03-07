/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf;

import io.frinx.cli.iosxr.ospf.handler.AreaInterfaceMplsSyncConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.mpls.igp.ldp.sync.ConfigBuilder;

public class AreaInterfaceMplsSyncConfigReaderTest {

    private final String outputDisable =
            "  interface GigabitEthernet0/0/0/3\n" +
            "   cost 100\n" +
            "   mpls ldp sync disable\n";

    private final String outputEnable =
            "  interface GigabitEthernet0/0/0/3\n" +
            "   cost 100\n" +
            "   mpls ldp sync\n";

    private final String outputNotSet =
            "  interface GigabitEthernet0/0/0/3\n" +
            "   cost 100\n";

    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputDisable,  builder);
        Assert.assertFalse(builder.isEnabled());

        builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputEnable,  builder);
        Assert.assertTrue(builder.isEnabled());

        builder = new ConfigBuilder();
        AreaInterfaceMplsSyncConfigReader.parseMplsSync(outputNotSet,  builder);
        Assert.assertNull(builder.isEnabled());
    }
}

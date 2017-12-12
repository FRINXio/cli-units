/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.snmp;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.iosxr.snmp.handler.EnabledTrapForEventReader;
import io.frinx.cli.unit.iosxr.snmp.handler.InterfaceReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEventBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceKey;


public class InterfaceReaderTest {

    private static final String OUTPUT = "Tue Dec 12 12:32:42.606 UTC\n" +
            "ifName : Null0                 linkUp/Down: enable\n" +
            "ifName : GigabitEthernet0/0/0/0  linkUp/Down: enable\n" +
            "ifName : GigabitEthernet0/0/0/1  linkUp/Down: enable\n" +
            "ifName : GigabitEthernet0/0/0/2  linkUp/Down: enable\n" +
            "ifName : GigabitEthernet0/0/0/3  linkUp/Down: enable\n" +
            "ifName : GigabitEthernet0/0/0/4  linkUp/Down: disable\n" +
            "ifName : GigabitEthernet0/0/0/5  linkUp/Down: enable\n";

    private static final String OUTPUT1 = "Tue Dec 12 12:32:42.606 UTC\n" +
            "ifName : Null0                 linkUp/Down: enable\n";

    private static final String OUTPUT2 = "Tue Dec 12 12:32:42.606 UTC\n" +
            "ifName : GigabitEthernet0/0/0/4  linkUp/Down: disable\n";

    @Test
    public void test() {
        Assert.assertArrayEquals(Lists.newArrayList("Null0", "GigabitEthernet0/0/0/0", "GigabitEthernet0/0/0/1", "GigabitEthernet0/0/0/2",
                "GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/4", "GigabitEthernet0/0/0/5").toArray(),
                InterfaceReader.parseInterfaceIds(OUTPUT).stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).toArray());
    }

    @Test
    public void testConfig() {
        EnabledTrapForEventBuilder builder = new EnabledTrapForEventBuilder();
        EnabledTrapForEventReader.parseEvent(OUTPUT1, builder);
        Assert.assertEquals(LINKUPDOWN.class, builder.getEventName());

        EnabledTrapForEventBuilder builder1 = new EnabledTrapForEventBuilder();
        EnabledTrapForEventReader.parseEvent(OUTPUT2, builder1);
        Assert.assertNull(builder1.getEventName());
    }
}

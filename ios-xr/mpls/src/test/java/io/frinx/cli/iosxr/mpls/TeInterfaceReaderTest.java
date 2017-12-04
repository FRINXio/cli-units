/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.mpls.handler.TeInterfaceReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te._interface.attributes.top.InterfaceKey;

import java.util.List;
import java.util.stream.Collectors;

public class TeInterfaceReaderTest {

    private static final String OUTPUT = "Tue Nov 28 14:43:38.325 UTC\n" +
        "  Link ID:: GigabitEthernet0/0/0/3 (192.168.111.10)\n" +
        "  Link ID:: GigabitEthernet0/0/0/4 (0.0.0.0)\n";

    @Test
    public void testIds() {
        List<InterfaceKey> keys = TeInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("GigabitEthernet0/0/0/3", "GigabitEthernet0/0/0/4"),
            keys.stream().map(InterfaceKey::getInterfaceId).map(InterfaceId::getValue).collect(Collectors.toList()));
    }
}

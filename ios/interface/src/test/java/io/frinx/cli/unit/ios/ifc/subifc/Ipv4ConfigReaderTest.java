/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.subifc;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

import static org.junit.Assert.assertEquals;

public class Ipv4ConfigReaderTest {

    @Test
    public void testParse() throws Exception {
        ConfigBuilder actual = new ConfigBuilder();
        Ipv4ConfigReader.parseAddressConfig(actual, "  Internet address is 192.168.1.44/24");
        assertEquals(new ConfigBuilder()
                .setIp(new Ipv4AddressNoZone("192.168.1.44"))
                .setPrefixLength((short) 24)
                .build(),
                actual.build());
    }

}
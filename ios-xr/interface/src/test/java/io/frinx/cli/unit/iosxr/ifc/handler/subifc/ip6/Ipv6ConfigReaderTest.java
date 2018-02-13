/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;


import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6ConfigReaderTest {

    private static final String SH_RUN_INT_IPV6 = "Mon Feb 12 13:25:08.172 UTC\n" +
            " ipv6 address fe80::260:2eef:fe11:6770 link-local\n" +
            " ipv6 address 2001:db8:a0b:12f0::1/72";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setPrefixLength((short)72)
            .setIp(new Ipv6AddressNoZone("2001:db8:a0b:12f0::1"))
            .build();

    @Test
    public void testParseAddressConfig() {
        ConfigBuilder actualConfigBuilder = new ConfigBuilder();
        Ipv6AddressNoZone address = new Ipv6AddressNoZone("2001:db8:a0b:12f0::1");
        Ipv6ConfigReader.parseAddressConfig(actualConfigBuilder, SH_RUN_INT_IPV6, address);

        Assert.assertEquals(EXPECTED_CONFIG, actualConfigBuilder.build());
    }
}
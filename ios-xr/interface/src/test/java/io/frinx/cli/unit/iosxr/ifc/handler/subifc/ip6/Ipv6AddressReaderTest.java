/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip6;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.AddressKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;

public class Ipv6AddressReaderTest {

    private static final String SH_RUN_INT_IPV6 = "Mon Feb 12 13:25:08.172 UTC\n" +
            " ipv6 address fe80::260:3eff:fe11:6770 link-local\n" +
            " ipv6 address 2001:db8:a0b:12f0::1/64";

    private static final List<AddressKey> EXPECTED_ADRESS_IDS =
            Lists.newArrayList("fe80::260:3eff:fe11:6770", "2001:db8:a0b:12f0::1")
            .stream()
            .map(Ipv6AddressNoZone::new)
            .map(AddressKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseAddressIds() {
        Assert.assertEquals(EXPECTED_ADRESS_IDS, Ipv6AddressReader.parseAddressIds(SH_RUN_INT_IPV6));
    }
}
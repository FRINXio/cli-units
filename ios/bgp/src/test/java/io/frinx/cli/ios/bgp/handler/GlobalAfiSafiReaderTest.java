/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;

public class GlobalAfiSafiReaderTest {

    private static final String BGP_OUTPUT = "router bgp 65000\n" +
            " address-family ipv4\n" +
            " address-family ipv4 vrf a\n";

    @Test
    public void testParse() throws Exception {
        List<AfiSafiKey> aFamilies = GlobalAfiSafiReader.getAfiKeys(BGP_OUTPUT, "a");
        List<AfiSafiKey> bFamilies = GlobalAfiSafiReader.getAfiKeys(BGP_OUTPUT, "b");
        List<AfiSafiKey> defaultFamilies = GlobalAfiSafiReader.getDefaultAfiKeys(BGP_OUTPUT);

        AfiSafiKey ipv4Key = new AfiSafiKey(IPV4UNICAST.class);

        assertThat(defaultFamilies, hasItems(ipv4Key));
        assertThat(aFamilies, hasItems(ipv4Key));
        assertTrue(bFamilies.isEmpty());

    }
}


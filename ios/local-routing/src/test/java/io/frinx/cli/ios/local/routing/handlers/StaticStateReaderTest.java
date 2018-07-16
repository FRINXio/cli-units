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

package io.frinx.cli.ios.local.routing.handlers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StaticStateReaderTest {

    private static final String IP_OUTPUT1 = "S        10.255.1.0 [1/0] via 192.168.1.5";
    private static final String IP_OUTPUT2 = "C        192.168.1.0/24 is directly connected, GigabitEthernet1";
    private static final String IP_OUTPUT3 = "";
    private static final String IPV6_OUTPUT1 = "S        2001:DB8:3000:0/16 [200/45]";



    @Test
    public void testParseStaticPrefixes() {
        assertTrue(StaticStateReader.isPrefixStatic(IP_OUTPUT1));
        assertFalse(StaticStateReader.isPrefixStatic(IP_OUTPUT2));
        assertFalse(StaticStateReader.isPrefixStatic(IP_OUTPUT3));
        assertTrue(StaticStateReader.isPrefixStatic(IPV6_OUTPUT1));
    }
}

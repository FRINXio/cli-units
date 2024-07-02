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

package io.frinx.cli.unit.brocade.cdp.handler;

import org.junit.jupiter.api.Test;

class InterfaceReaderTest {

    @Test
    void testParseCdpIfcs() throws Exception {
        InterfaceReader.parseCdpInterfaces("""
                interface loopback 1
                interface loopback 2
                interface management 1
                interface ethernet 1/1
                interface ethernet 1/2
                interface ethernet 1/3
                interface ethernet 1/4
                interface ethernet 1/5
                interface ethernet 1/6
                interface ethernet 1/7
                interface ethernet 1/8
                interface ethernet 1/9
                interface ethernet 1/10
                interface ethernet 1/11
                interface ethernet 1/12
                interface ethernet 1/13
                interface ethernet 1/14
                interface ethernet 1/15
                interface ethernet 1/17
                interface ethernet 1/18
                interface ethernet 1/19
                 no cdp enable
                interface ethernet 1/20
                interface ethernet 2/1                                           \s
                interface ethernet 2/2
                 no cdp enable
                interface ethernet 2/3
                interface ethernet 2/4
                interface ethernet 2/5
                interface ethernet 2/6
                interface ethernet 2/7
                interface ethernet 2/8
                interface ethernet 2/9
                interface ethernet 2/10
                 no cdp enable
                interface ethernet 2/11
                 no cdp enable
                interface ethernet 2/12
                 no cdp enable
                interface ethernet 2/13
                interface ethernet 2/14
                 no cdp enable
                interface ethernet 2/15
                interface ethernet 2/16
                interface ethernet 2/17
                 no cdp enable
                interface ethernet 2/18                                          \s
                 no cdp enable
                interface ethernet 2/19
                 no cdp enable
                interface ethernet 2/20
                 no cdp enable
                interface ethernet 3/1
                interface ethernet 3/6
                interface ethernet 3/7
                interface ethernet 3/8
                interface ethernet 3/10
                interface ethernet 3/14
                interface ethernet 3/15
                interface ethernet 3/16
                interface ethernet 3/17
                interface ethernet 3/18
                interface ethernet 3/19
                interface ethernet 3/20
                interface ethernet 4/1
                 no cdp enable
                interface ethernet 4/2
                 no cdp enable
                interface ve 3
                interface ve 4                                                   \s
                interface ve 7
                interface ve 9
                interface ve 12
                interface ve 32
                interface ve 44
                interface ve 77
                interface ve 100
                interface ve 112
                interface ve 150
                interface ve 200
                interface ve 210
                interface ve 212
                """);
    }
}
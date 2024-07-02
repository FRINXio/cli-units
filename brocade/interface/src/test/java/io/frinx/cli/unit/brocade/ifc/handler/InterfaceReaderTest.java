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

package io.frinx.cli.unit.brocade.ifc.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.frinx.cli.io.Cli;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

class InterfaceReaderTest {

    private static final String OUTPUT = """
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
            interface ethernet 1/16
            interface ethernet 1/17
            interface ethernet 1/18
            interface ethernet 1/19
            interface ethernet 1/20
            interface ethernet 1/21
            interface ethernet 1/22
            interface ethernet 1/23
            interface ethernet 1/24
            interface ethernet 2/1
            interface ethernet 2/2
            interface ve 12
            interface ve 33
            interface ve 30
            interface ve 111
            interface ve 112
            interface loopback 1""";

    @Test
    void testAllIds() {
        List<InterfaceKey> interfaceKeys = new InterfaceReader(Mockito.mock(Cli.class)).parseInterfaceIds(OUTPUT);
        assertThat(interfaceKeys, CoreMatchers.hasItem(new InterfaceKey("ve 12")));
    }
}
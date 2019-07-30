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

import io.frinx.cli.io.Cli;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "interface ethernet 1/1\n"
            + "interface ethernet 1/2\n"
            + "interface ethernet 1/3\n"
            + "interface ethernet 1/4\n"
            + "interface ethernet 1/5\n"
            + "interface ethernet 1/6\n"
            + "interface ethernet 1/7\n"
            + "interface ethernet 1/8\n"
            + "interface ethernet 1/9\n"
            + "interface ethernet 1/10\n"
            + "interface ethernet 1/11\n"
            + "interface ethernet 1/12\n"
            + "interface ethernet 1/13\n"
            + "interface ethernet 1/14\n"
            + "interface ethernet 1/15\n"
            + "interface ethernet 1/16\n"
            + "interface ethernet 1/17\n"
            + "interface ethernet 1/18\n"
            + "interface ethernet 1/19\n"
            + "interface ethernet 1/20\n"
            + "interface ethernet 1/21\n"
            + "interface ethernet 1/22\n"
            + "interface ethernet 1/23\n"
            + "interface ethernet 1/24\n"
            + "interface ethernet 2/1\n"
            + "interface ethernet 2/2\n"
            + "interface ve 12\n"
            + "interface ve 33\n"
            + "interface ve 30\n"
            + "interface ve 111\n"
            + "interface ve 112\n"
            + "interface loopback 1";

    @Test
    public void testAllIds() {
        List<InterfaceKey> interfaceKeys = new InterfaceReader(Mockito.mock(Cli.class)).parseAllInterfaceIds(OUTPUT);
        Assert.assertThat(interfaceKeys, CoreMatchers.hasItem(new InterfaceKey("ve 12")));
    }
}
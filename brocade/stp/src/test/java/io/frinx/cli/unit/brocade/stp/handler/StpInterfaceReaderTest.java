/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.stp.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class StpInterfaceReaderTest {

    @Test
    public void parsingInterfaces() {
        String output = "interface ethernet 1/1\n"
                + "interface ethernet 1/2\n"
                + "interface ethernet 1/3\n"
                + "interface ethernet 1/4\n"
                + "interface ethernet 1/5\n"
                + " no spanning-tree\n"
                + "interface ethernet 1/6\n"
                + "interface ethernet 1/7\n"
                + "interface ethernet 1/8\n"
                + "interface ethernet 1/9\n"
                + "interface ethernet 1/11\n"
                + "interface ethernet 3/17\n"
                + " no spanning-tree\n"
                + "interface ethernet 3/18\n"
                + " no spanning-tree\n"
                + "interface ethernet 3/19\n"
                + " no spanning-tree\n"
                + "interface ethernet 3/20\n"
                + "interface ethernet 4/1\n"
                + " no spanning-tree\n"
                + "interface ethernet 4/2\n"
                + " no spanning-tree\n";

        StpInterfaceReader reader = new StpInterfaceReader(Mockito.mock(Cli.class));
        Assert.assertEquals(10, reader.parseInterfaceIds(output).size());
    }
}
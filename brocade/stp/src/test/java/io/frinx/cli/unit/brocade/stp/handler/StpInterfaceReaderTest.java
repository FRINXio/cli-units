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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class StpInterfaceReaderTest {

    @Test
    void parsingInterfaces() {
        String output = """
                interface ethernet 1/1
                interface ethernet 1/2
                interface ethernet 1/3
                interface ethernet 1/4
                interface ethernet 1/5
                 no spanning-tree
                interface ethernet 1/6
                interface ethernet 1/7
                interface ethernet 1/8
                interface ethernet 1/9
                interface ethernet 1/11
                interface ethernet 3/17
                 no spanning-tree
                interface ethernet 3/18
                 no spanning-tree
                interface ethernet 3/19
                 no spanning-tree
                interface ethernet 3/20
                interface ethernet 4/1
                 no spanning-tree
                interface ethernet 4/2
                 no spanning-tree
                """;

        StpInterfaceReader reader = new StpInterfaceReader(Mockito.mock(Cli.class));
        assertEquals(10, reader.parseInterfaceIds(output).size());
    }
}
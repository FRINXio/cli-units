/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.qos.handler.ifc;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces.InterfaceKey;

public class InterfaceReaderTest {

    private static final String OUTPUT = "traffic-profiling set port 1 mode advanced\n"
            + "traffic-profiling set port 3 mode advanced\n"
            + "traffic-profiling set port 4 mode advanced\n"
            + "traffic-profiling set port 6 mode advanced\n"
            + "traffic-profiling standard-profile set port 1 profile CIA_CoS0\n"
            + "traffic-profiling enable port 1\n"
            + "traffic-profiling enable port 8\n"
            + "traffic-profiling enable\n";

    @Test
    public void getAllIdsTest() {
        List<InterfaceKey> ids = Arrays.asList(new InterfaceKey("1"),
                new InterfaceKey("3"),
                new InterfaceKey("4"),
                new InterfaceKey("6"),
                new InterfaceKey("8"));
        Assert.assertEquals(ids, InterfaceReader.getAllIds(OUTPUT));
    }
}

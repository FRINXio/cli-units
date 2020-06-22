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

package io.frinx.cli.unit.saos.logical.ring.handler.virtual;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.rev200622.virtual.ring.top.virtual.rings.VirtualRingKey;


public class VirtualRingReaderTest {

    private static final String OUTPUT =
            "ring-protection virtual-ring create virtual-ring-name v-ring-test1 logical-ring l-ring-test1 "
            + "raps-vid 101\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring-test2 logical-ring l-ring-test2 "
            + "raps-vid 102\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring-test3 logical-ring l-ring-test2 "
            + "raps-vid 103\n"
            + "ring-protection virtual-ring create virtual-ring-name frinx_test logical-ring l-ring-test2 "
            + "raps-vid 55\n"
            + "ring-protection virtual-ring create virtual-ring-name frinx_test2 logical-ring l-ring-test2 "
            + "raps-vid 56\n"
            + "ring-protection virtual-ring create virtual-ring-name v-ring-12 logical-ring LOGICAL_FRINX3 "
            + "raps-vid 4094";

    @Test
    public void getAllIdsTest() {
        List<VirtualRingKey> ringKeys = Arrays.asList(
                new VirtualRingKey("v-ring-test2"),
                new VirtualRingKey("v-ring-test3"),
                new VirtualRingKey("frinx_test"),
                new VirtualRingKey("frinx_test2")
        );

        Assert.assertEquals(ringKeys, VirtualRingReader.getAllIds(OUTPUT, "l-ring-test2"));
    }
}

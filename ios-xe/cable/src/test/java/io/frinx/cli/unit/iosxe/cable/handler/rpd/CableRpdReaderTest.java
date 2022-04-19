/*
 * Copyright © 2022 Frinx and others.
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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.RpdKey;

public class CableRpdReaderTest {

    private static final String OUTPUT = "cable rpd VFZ-RPD-100\n"
            + "cable rpd VFZ-RPD-101\n"
            + "cable rpd VFZ-RPD-120\n"
            + "cable rpd VFZ-RPD-121\n"
            + "cable rpd VFZ-RPD-140\n"
            + "cable rpd VFZ-RPD-141\n"
            + "cable rpd VFZ-RPD-160\n"
            + "cable rpd VFZ-RPD-161\n"
            + "cable rpd VFZ-RPD-162\n"
            + "cable rpd VFZ-RPD-163\n"
            + "cable rpd VFZ-RPD-164\n"
            + "cable rpd VFZ-RPD-165\n"
            + "cable rpd VFZ-RPD-166\n";

    @Test
    public void testGetIds() {
        List<RpdKey> keys = CableRpdReader.getCableRpds(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("VFZ-RPD-100", "VFZ-RPD-101", "VFZ-RPD-120", "VFZ-RPD-121",
                        "VFZ-RPD-140", "VFZ-RPD-141", "VFZ-RPD-160", "VFZ-RPD-161", "VFZ-RPD-162", "VFZ-RPD-163",
                        "VFZ-RPD-164", "VFZ-RPD-165", "VFZ-RPD-166"),
                keys.stream().map(RpdKey::getId).collect(Collectors.toList()));
    }
}

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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ring;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ring.saos.rev200317.saos.virtual.ring.extension.virtual.rings.VirtualRingKey;

public class VirtualRingReaderTest {

    private static final String OUTPUT =
            "ring-protection virtual-ring add ring VSR990101 vs FRINX002_2501\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX003_2502\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX004_2503\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX005_2504\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX006_2505\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX007_2506\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX008_2507\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX009_2508\n"
            + "ring-protection virtual-ring add ring VSR990101 vs FRINX010_2509\n"
            + "ring-protection virtual-ring add ring VSR990101 vs 3d_party-test\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX001_2500\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX002_2501\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX003_2502\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX004_2503\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX005_2504\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX006_2505\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX007_2506\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX008_2507\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX009_2508\n"
            + "ring-protection virtual-ring add ring VSR990102 vs FRINX010_2509\n";

    @Test
    public void getAllIdsTest() {
        List<VirtualRingKey> ringKeys = Arrays.asList(
                new VirtualRingKey("VSR990101"),
                new VirtualRingKey("VSR990102"));

        Assert.assertEquals(ringKeys, VirtualRingReader.getAllIds(OUTPUT, "FRINX007_2506"));
    }
}

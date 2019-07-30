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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public class L2VSIReaderTest {

    private static String OUTPUT = " vpls test 12\n"
            + " vpls test2 13\n"
            + " vpls tese2 14\n"
            + " vpls tesr2 15\n"
            + " vpls tesf2 16\n"
            + " vpls testG2 17\n"
            + " vpls tesj2 19\n";

    @Test
    public void readTest() {
        List<NetworkInstanceKey> keys = L2VSIReader.parseL2Vsis(OUTPUT);
        Assert.assertEquals(7, keys.size());
    }
}
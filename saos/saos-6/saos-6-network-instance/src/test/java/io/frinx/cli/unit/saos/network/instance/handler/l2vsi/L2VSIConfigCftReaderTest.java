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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.l2.cft.extension.cfts.CftKey;

public class L2VSIConfigCftReaderTest {

    private static final String OUTPUT = "l2-cft create profile L2Test2 tunnel-method l2pt\n"
            + "l2-cft create profile CTB\n"
            + "l2-cft create profile VS11\n"
            + "l2-cft create profile MY\n"
            + "l2-cft create profile CTA\n"
            + "l2-cft create profile DAAN-1\n"
            + "l2-cft create profile VLAN111222\n";

    @Test
    public void getAllIdsTest() {
        List<CftKey> cftKeys = Arrays.asList(new CftKey("L2Test2"),
                new CftKey("CTB"),
                new CftKey("VS11"),
                new CftKey("MY"),
                new CftKey("CTA"),
                new CftKey("DAAN-1"),
                new CftKey("VLAN111222"));

        Assert.assertEquals(cftKeys, L2VSIConfigCftReader.getAllIds(OUTPUT));
    }
}

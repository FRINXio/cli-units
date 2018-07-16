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

package io.frinx.cli.unit.ios.network.instance.handler.vrf;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

@RunWith(MockitoJUnitRunner.class)
public class VrfReaderTest {

    private static final String SH_IP_VRF = "ip vrf DEP_1  \n"
            + "ip vrf DEP_2  dfs dsf dsf\n"
            + "ip vrf a\n";

    private static final List<NetworkInstanceKey> IDS_EXPECTED =
            Lists.newArrayList("DEP_1", "DEP_2", "a", "default")
                    .stream()
                    .map(NetworkInstanceKey::new)
                    .collect(Collectors.toList());

    @Test
    public void testReader() {
        assertEquals(IDS_EXPECTED, VrfReader.parseVrfIds(SH_IP_VRF));
    }

}
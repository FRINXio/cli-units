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

package io.frinx.cli.ospf.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;

public class OspfAreaReaderTest {

    public static final String OSPF_1 = " ip ospf 1 area 0\n"
            + " ip ospf 1 area 48\n"
            + " ip ospf 1 area 0.0.0.0\n"
            + " ip ospf 1 area 9.9.9.9\n";

    public static final List<AreaKey> AREAS = Lists.newArrayList("0", "48", "0.0.0.0", "9.9.9.9")
            .stream()
            .map(OspfAreaReader::getAreaIdentifier)
            .map(AreaKey::new)
            .collect(Collectors.toList());

    @Test
    public void testParseArea() throws Exception {
        List<AreaKey> areaKeys = OspfAreaReader.parseAreasIds(OSPF_1);
        Assert.assertEquals(AREAS, areaKeys);
    }
}
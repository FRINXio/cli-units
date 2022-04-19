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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102._if.rpd.us.top._if.rpd.us.UpstreamPortsKey;

public class CoreInterfaceUpstreamReaderTest {

    private static final String OUTPUT = "cable rpd VFZ-RPD-100\n"
            + "  rpd-us 0 upstream-cable 1/0/0 profile 5\n"
            + "  rpd-us 1 upstream-cable 1/0/1 profile 5\n"
            + "cable rpd VFZ-RPD-101\n"
            + "  rpd-us 0 upstream-cable 1/0/2 profile 5\n"
            + "  rpd-us 1 upstream-cable 1/0/3 profile 5\n"
            + "cable rpd VFZ-RPD-120\n"
            + "  rpd-us 0 upstream-cable 1/0/4 profile 4\n"
            + "  rpd-us 1 upstream-cable 1/0/5 profile 4\n"
            + "cable rpd VFZ-RPD-121\n"
            + "  rpd-us 0 upstream-cable 1/0/6 profile 4\n"
            + "  rpd-us 1 upstream-cable 1/0/7 profile 4\n";

    @Test
    public void testGetIds() {
        List<UpstreamPortsKey> keys = CoreInterfaceUpstreamReader.getCoreIfUpstream(OUTPUT, "VFZ-RPD-100");
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("0", "1"),
                keys.stream().map(UpstreamPortsKey::getId).collect(Collectors.toList()));
    }
}

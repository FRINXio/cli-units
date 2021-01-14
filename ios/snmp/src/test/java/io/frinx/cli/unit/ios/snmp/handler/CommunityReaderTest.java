/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.ios.snmp.handler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.community.top.communities.CommunityKey;

public class CommunityReaderTest {

    private static final String OUTPUT = "snmp-server community Foo view SnmpReadAccess RW\n"
            + "snmp-server community Bar view TESTview RO 70";

    @Test
    public void testViewKeys() {
        final List<CommunityKey> keys = CommunityReader.getCommunityKeys(OUTPUT);
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals(Lists.newArrayList("Foo", "Bar"),
                keys.stream().map(CommunityKey::getName).collect(Collectors.toList()));
    }

}
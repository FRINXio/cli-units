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

package io.frinx.cli.unit.cubro.unit.acl;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.cubro.unit.acl.handler.IngressAclSetReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;

public class IngressAclSetReaderTest {

    private static String OUTPUT = "interface elag 10\n"
        + "interface egroup 1\n"
        + "interface egroup 10\n"
        + "interface 4 \n"
        + "    mtu 2500\n"
        + "    apply access-list ip FAS in\n"
        + "interface 9 comment ahoj 09\n"
        + "interface 9 \n"
        + "    rx on\n"
        + "    speed 100000\n"
        + "    mtu 1760\n"
        + "    vxlanterminated enable\n"
        + "interface 10 \n"
        + "    apply access-list ip Matotest in\n"
        + "interface 32 \n"
        + "    rx on\n"
        + "    speed 100000\n"
        + "    apply access-list ip FAS in\n"
        + "access-list ipv4 Matotest comment matotest\n"
        + "access-list ipv4 Matotest\n"
        + "    900 permit any any any \n"
        + "access-list ipv4 FAS\n"
        + "    999 forward egroup 1 any any any \n";

    @Test
    public void testGetAllIds() {
        List<String> expected = Lists.newArrayList("FAS", "Matotest");
        Assert.assertEquals(expected, IngressAclSetReader.parseAclKeys(OUTPUT)
                .stream().map(IngressAclSetKey::getSetName).collect(Collectors.toList()));
    }
}

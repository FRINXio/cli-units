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

package io.frinx.cli.unit.saos.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;

class IngressAclSetReaderTest {

    private static final String OUTPUT = """
            port set port 5 max-frame-size 1998 ingress-acl FOO
            port set port 5 untagged-data-vs VLAN1 untagged-ctrl-vs VLAN1
            """;

    @Test
    void testIds() {
        final List<IngressAclSetKey> setKeys = IngressAclSetReader.parseAclKeys(OUTPUT);
        assertEquals(1, setKeys.size());
        assertEquals("FOO", setKeys.get(0).getSetName());
        assertEquals(ACLIPV4.class, setKeys.get(0).getType());
    }

}
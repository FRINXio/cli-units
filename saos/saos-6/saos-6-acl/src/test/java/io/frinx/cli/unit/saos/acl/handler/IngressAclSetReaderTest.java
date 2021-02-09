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

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;

public class IngressAclSetReaderTest {

    private static final String OUTPUT = "port set port 5 max-frame-size 1998 ingress-acl FOO\n"
            + "port set port 5 untagged-data-vs VLAN1 untagged-ctrl-vs VLAN1\n";

    @Test
    public void testIds() {
        final List<IngressAclSetKey> setKeys = IngressAclSetReader.parseAclKeys(OUTPUT);
        Assert.assertEquals(1, setKeys.size());
        Assert.assertEquals("FOO", setKeys.get(0).getSetName());
        Assert.assertEquals(ACLTYPE.class, setKeys.get(0).getType());
    }

}
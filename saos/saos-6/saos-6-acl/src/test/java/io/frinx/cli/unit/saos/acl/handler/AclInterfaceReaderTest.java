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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.InterfaceKey;

public class AclInterfaceReaderTest {

    private static final String OUTPUT = "port set port 7 max-frame-size 1998 ingress-acl FOO\n"
            + "port set port LAG_CUST ingress-acl BAR\n"
            + "port set port 15 ingress-acl FOOBAR\n";

    @Test
    public void testIds() {
        final List<InterfaceKey> interfaceKeys = AclInterfaceReader.getInterfaceKeys(OUTPUT);
        Assert.assertEquals(3, interfaceKeys.size());
        Assert.assertEquals("7", interfaceKeys.get(0).getId().getValue());
        Assert.assertEquals("LAG_CUST", interfaceKeys.get(1).getId().getValue());
        Assert.assertEquals("15", interfaceKeys.get(2).getId().getValue());
    }

}
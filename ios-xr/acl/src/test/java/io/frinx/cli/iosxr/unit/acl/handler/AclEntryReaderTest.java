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

package io.frinx.cli.iosxr.unit.acl.handler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;

public class AclEntryReaderTest {

    @Test
    public void test() {
        List<AclEntryKey> result = AclEntryReader.parseAclEntryKey("Fri Feb 23 15:25:27.410 UTC\n"
                + "ipv4 access-list ipv4foo\n"
                + " 1 permit ipv4 any any\n"
                + " 10 remark remark1\n"
                + "!");
        assertEquals(Arrays.asList(new AclEntryKey(1L)), result);
    }

}

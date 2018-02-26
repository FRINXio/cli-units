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
import static org.junit.Assert.assertNull;

import io.frinx.openconfig.openconfig.acl.IIDs;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACCEPT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.DROP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntry;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.access.list.entries.top.acl.entries.AclEntryKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AclEntryActionsConfigReaderTest {

    @Test
    public void testTryToParseForwardingAction() {
        String lines = "ipv4 access-list foo\n" +
                " 2 deny ipv4 host 1.2.3.4 any\n" +
                " 3 permit udp 192.168.1.1/24 10.10.10.10/24\n" +
                " 4 remark remark\n" +
                "!\n";

        assertEquals(DROP.class, AclEntryActionsConfigReader.tryToParseForwardingAction(mockId(2l), lines));
        assertEquals(ACCEPT.class, AclEntryActionsConfigReader.tryToParseForwardingAction(mockId(3l), lines));
        assertNull(AclEntryActionsConfigReader.tryToParseForwardingAction(mockId(4l), lines));
    }

    private InstanceIdentifier mockId(long sequenceId) {

        AclEntryKey key = new AclEntryKey(sequenceId);
        return IIDs.AC_AC_AC_ACLENTRIES.builder().child(AclEntry.class, key).build();
    }
}

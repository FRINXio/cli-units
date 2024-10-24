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

package io.frinx.cli.unit.saos.acl.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;

class AclSetReaderTest {

    static final String OUTPUT = """
            access-list create acl-profile ACL_TEMPLATE_EVPN_v017 default-filter-action allow
            access-list disable profile ACL_TEMPLATE_EVPN_v017
            access-list create acl-profile CPE_MGT_TEMPLATE_v08 default-filter-action deny
            access-list create acl-profile foo default-filter-action allow
            access-list add profile ACL_TEMPLATE_EVPN_v017 rule VLAN399918 precedence 1 filter-action allow any
            access-list add profile foo rule bar precedence 1 filter-action allow any
            access-list add profile foo rule test1 precedence 2 filter-action allow any
            access-list add profile foo rule test3 precedence 3 filter-action deny any
            """;

    static final String OUTPUT_74 = """
            access-list set filter-mode l3-only
            access-list create acl-profile MGT-IN default-filter-action allow
            access-list add profile MGT-IN rule Allow-OSS-MGT precedence 10 filter-action allow src-ip 172.22.128.0/18
            access-list add profile MGT-IN rule Allow-TESTLAB precedence 20 filter-action allow src-ip 172.16.0.0/23
            access-list add profile MGT-IN rule ALLOW-1C precedence 30 filter-action allow src-ip 172.22.160.115
            """;

    @Test
    void getAllIdsTest() {
        List<AclSetKey> expected = Arrays.asList(
                new AclSetKey("ACL_TEMPLATE_EVPN_v017", ACLIPV4.class),
                new AclSetKey("CPE_MGT_TEMPLATE_v08", ACLIPV4.class),
                new AclSetKey("foo", ACLIPV4.class));

        assertEquals(expected, AclSetReader.getAllIds(OUTPUT));

        assertEquals(Lists.newArrayList(new AclSetKey("MGT-IN", ACLIPV4.class)),
                AclSetReader.getAllIds(OUTPUT_74));
    }
}

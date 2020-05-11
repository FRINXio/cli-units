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

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.set.top.acl.sets.AclSetKey;

public class AclSetReaderTest {

    static final String OUTPUT =
        "access-list create acl-profile ACL_TEMPLATE_EVPN_v017 default-filter-action allow\n"
        + "access-list disable profile ACL_TEMPLATE_EVPN_v017\n"
        + "access-list create acl-profile CPE_MGT_TEMPLATE_v08 default-filter-action deny\n"
        + "access-list create acl-profile foo default-filter-action allow\n"
        + "access-list add profile ACL_TEMPLATE_EVPN_v017 rule VLAN399918 precedence 1 filter-action allow any\n"
        + "access-list add profile foo rule bar precedence 1 filter-action allow any\n"
        + "access-list add profile foo rule test1 precedence 2 filter-action allow any\n"
        + "access-list add profile foo rule test3 precedence 3 filter-action deny any\n";

    @Test
    public void getAllIdsTest() {
        List<AclSetKey> expected = Arrays.asList(
                new AclSetKey("ACL_TEMPLATE_EVPN_v017", ACLTYPE.class),
                new AclSetKey("CPE_MGT_TEMPLATE_v08", ACLTYPE.class),
                new AclSetKey("foo", ACLTYPE.class));

        Assert.assertEquals(expected, AclSetReader.getAllIds(OUTPUT));
    }
}

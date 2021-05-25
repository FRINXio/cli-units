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

package io.frinx.cli.unit.ios.routing.policy.handlers.statement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;

public class StatementReaderTest {

    private static final String OUTPUT =
            "route-map RM_CI_REDIST_STATIC_VLAN123456_V6 permit 100 \n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V6 permit 300 \n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V6 deny 1000 \n"
            + "route-map RM_CI_ANTIDDOS_VLAN123456_DENY_ALL_V6 deny 0 \n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V4 permit 100 \n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V4 permit 300 \n"
            + "route-map RM_CI_REDIST_STATIC_VLAN123456_V4 deny 1000 \n"
            + "route-map FRINX permit 10 \n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 permit 100 \n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 permit 200 \n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 permit 300 \n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 permit 400 \n"
            + "route-map RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4 deny 1000 \n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_SEC_PE_V4 permit 100 \n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_SEC_PE_V4 permit 200 \n"
            + "route-map RM_CI_VLAN113399_PRI_CPE_SEC_PE_V4 deny 1000 \n"
            + "route-map RM_CI_VLAN112233_PRI_PE_V6 permit 100 \n";

    @Test
    public void getAllIdsTest() {
        List<StatementKey> expected = Stream.of("100", "200", "300", "400", "1000")
                .map(StatementKey::new)
                .collect(Collectors.toList());

        Assert.assertEquals(expected, StatementReader.getAllIds(OUTPUT, "RM_CI_VLAN112233_SEC_CPE_PRI_PE_V4"));
    }
}

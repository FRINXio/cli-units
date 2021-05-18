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

package io.frinx.cli.unit.ios.routing.policy.handlers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.policy.statements.top.statements.StatementKey;

public class StatementReaderTest {

    private static final String OUTPUT =
            "neighbor 217.105.224.26 route-map RM-IPVPN-SECONDARY-PE in\n"
            + "route-map RM-IPVPN-SECONDARY-PE permit 9 \n"
            + "route-map RM-IPVPN-SECONDARY-PE permit 10 \n";

    @Test
    public void getAllIdsTest() {
        List<StatementKey> expected = Stream.of("9", "10")
                .map(StatementKey::new).collect(Collectors.toList());

        Assert.assertEquals(expected, StatementReader.getAllIds(OUTPUT));
    }
}

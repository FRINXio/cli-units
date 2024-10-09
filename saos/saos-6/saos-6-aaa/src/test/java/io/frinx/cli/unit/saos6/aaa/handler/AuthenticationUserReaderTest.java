/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.saos6.aaa.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.rev200730.aaa.authentication.user.top.users.UserKey;

class AuthenticationUserReaderTest {

    private static final String OUTPUT  =
            """
                    +------------------------------- USER ACCOUNT TABLE -------------------------------+
                    | Username                         | Privilege  | Sessions | Sess. Limit | Lockout |
                    +----------------------------------+------------+----------+-------------+---------+
                    | abc                              | diag       |     0    |      -      |         |
                    | def                              | super      |     0    |      -      |         |
                    | ghg                              | limited    |     0    |      -      |         |
                    +----------------------------------+------------+----------+-------------+---------+
                    """;

    @Test
    void parseAllUserKeysTest() {
        final var userKeys = AuthenticationUserReader.parseAllUserKeys(OUTPUT);
        final var expectedKeys = List.of(
                new UserKey("abc"),
                new UserKey("def"),
                new UserKey("ghg")
        );
        assertEquals(expectedKeys, userKeys);
    }
}

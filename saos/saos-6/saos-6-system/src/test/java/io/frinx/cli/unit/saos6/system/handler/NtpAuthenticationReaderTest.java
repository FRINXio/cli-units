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

package io.frinx.cli.unit.saos6.system.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.NtpKeyKey;

public class NtpAuthenticationReaderTest {

    public static final String OUTPUT  = """
            +-- NTP AUTHENTICATION KEYS --+
            | Key ID       | Type         |
            +--------------+--------------+
            | 1            | sha1         |
            +--------------+--------------+""";

    @Test
    void parseAllKeyIdsTest() {

        final var keyIds = NtpAuthenticationReader.parseAllKeyIds(OUTPUT);
        final var expectedKeyIds = List.of(
                new NtpKeyKey(1)
        );
        assertEquals(expectedKeyIds, keyIds);
    }
}

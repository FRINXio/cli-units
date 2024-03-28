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

package io.frinx.cli.unit.saos8.system.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaServerAuthenticationAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.ciena.ntp.extension.rev221104.CienaSystemAuthenticationExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.rev210923.system.ntp.auth.keys.top.ntp.keys.ntp.key.StateBuilder;

class NtpAuthenticationStateReaderTest {

    @Test
    void parseServerStateTest() {
        var stateBuilder = new StateBuilder();
        var builder = new CienaServerAuthenticationAugBuilder();
        NtpAuthenticationStateReader.parseKeyState(stateBuilder, builder, NtpAuthenticationReaderTest.OUTPUT);

        var expectedState = new StateBuilder()
                .setKeyId(1);
        var expectedAug = new CienaServerAuthenticationAugBuilder()
                .setKeyType(CienaSystemAuthenticationExtension.KeyType.SHA1);
        assertEquals(expectedState.build(), stateBuilder.build());
        assertEquals(expectedAug.build(), builder.build());
    }
}

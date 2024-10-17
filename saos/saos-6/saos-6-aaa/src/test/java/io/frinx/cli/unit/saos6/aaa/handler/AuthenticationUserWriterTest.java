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

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthenticationUserWriterTest {

    private final AuthenticationUserWriter writer = new AuthenticationUserWriter(Mockito.mock(Cli.class));

    @Test
    void writeTemplate() {
        assertEquals(
                "user create user abc access-level diag secret 123",
                writer.writeTemplate("abc", "diag", "123"));
    }

    @Test
    void deleteTemplate() {
        assertEquals(
                "user delete user abc",
                writer.deleteTemplate("abc"));
    }
}

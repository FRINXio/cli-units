/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.aaa.handler.authentication;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.authentication.schemes.AuthenticationKey;

public class AuthenticationSchemasReaderTest {

    private final String outputAuthentications = " authentication-scheme default\n"
            + " authentication-scheme radius\n"
            + " authentication-scheme AUT-ZIGGO\n"
            + "  authentication-scheme radius\n"
            + "  authentication-scheme AUT-ZIGGO\n";

    @Test
    public void testAuthenticationIds() {
        List<AuthenticationKey> keys = AuthenticationSchemasReader.getAllIds(outputAuthentications);
        Assert.assertEquals(Arrays.asList(new AuthenticationKey("default"),
                new AuthenticationKey("radius"), new AuthenticationKey("AUT-ZIGGO")), keys);
    }
}



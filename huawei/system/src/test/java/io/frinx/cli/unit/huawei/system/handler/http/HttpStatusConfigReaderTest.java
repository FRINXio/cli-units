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

package io.frinx.cli.unit.huawei.system.handler.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.http.status.extension.rev211028.huawei.http.server.http.server.status.ConfigBuilder;

class HttpStatusConfigReaderTest {
    private static final String HTTP_STATUS = """
            HTTP server status              : Enabled        (default: disable)
              HTTP server port                : 80             (default: 80)
              HTTP timeout interval           : 10             (default: 10 minutes)
              Current online users            : 0                  \s
              Maximum users allowed           : 5        \s
              HTTPS server status             : Enabled        (default: disable)
              HTTPS server port               : 443            (default: 443)
              HTTPS server manager port       :
              HTTPS SSL Policy                :\s
            """;

    @Test
    void parseConfigTest() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        HttpStatusConfigReader.parseConfig(HTTP_STATUS, configBuilder);
        Config expected = new ConfigBuilder().setHttpServerStatusEnabled(true).setHttpSecureServerStatusEnabled(true)
                .build();
        assertEquals(expected, configBuilder.build());
    }
}

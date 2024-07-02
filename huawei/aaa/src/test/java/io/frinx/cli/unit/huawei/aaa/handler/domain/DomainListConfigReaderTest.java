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

package io.frinx.cli.unit.huawei.aaa.handler.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.domain.ConfigBuilder;

class DomainListConfigReaderTest {

    private final String outputDomainConfig = """

             authentication-mode radius
              authentication-mode local radius
              accounting-mode radius
              accounting start-fail online
             domain default
              authentication-scheme radius
              radius-server default
             domain default_admin
              authentication-scheme AUT-ZIGGO
              accounting-scheme ACC-SC
              radius-server RADIUS-ZIGGO
            """;

    @Test
    void testDomainIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        DomainListConfigReader.parseConfigAttributes(outputDomainConfig, configBuilder, "default");
        assertEquals(new ConfigBuilder().setAuthenticationScheme("radius").setRadiusServer("default").build(),
                configBuilder.build());
    }

    @Test
    void testDomainIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        DomainListConfigReader.parseConfigAttributes(outputDomainConfig, configBuilder, "default_admin");
        assertEquals(new ConfigBuilder().setAccountingScheme("ACC-SC").setAuthenticationScheme("AUT-ZIGGO")
                        .setRadiusServer("RADIUS-ZIGGO").build(), configBuilder.build());
    }
}

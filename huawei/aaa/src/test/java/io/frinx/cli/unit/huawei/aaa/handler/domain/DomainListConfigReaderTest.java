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

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.huawei.aaa.extension.domain.list.domain.ConfigBuilder;

public class DomainListConfigReaderTest {

    private final String outputDomainConfig = "\n authentication-mode radius\n"
            + "  authentication-mode local radius\n"
            + "  accounting-mode radius\n"
            + "  accounting start-fail online\n"
            + " domain default\n"
            + "  authentication-scheme radius\n"
            + "  radius-server default\n"
            + " domain default_admin\n"
            + "  authentication-scheme AUT-ZIGGO\n"
            + "  accounting-scheme ACC-SC\n"
            + "  radius-server RADIUS-ZIGGO\n";

    @Test
    public void testDomainIdsWithoutConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        DomainListConfigReader.parseConfigAttributes(outputDomainConfig, configBuilder, "default");
        Assert.assertEquals(new ConfigBuilder().setAuthenticationScheme("radius").setRadiusServer("default").build(),
                configBuilder.build());
    }

    @Test
    public void testDomainIdsWithConfig() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        DomainListConfigReader.parseConfigAttributes(outputDomainConfig, configBuilder, "default_admin");
        Assert.assertEquals(new ConfigBuilder().setAccountingScheme("ACC-SC").setAuthenticationScheme("AUT-ZIGGO")
                        .setRadiusServer("RADIUS-ZIGGO").build(), configBuilder.build());
    }
}

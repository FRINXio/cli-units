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

package io.frinx.cli.unit.iosxe.bfd.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.BfdTemplateConfig.Type;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.config.bfd.template.grouping.bfd.templates.bfd.template.ConfigBuilder;

public class BfdTemplateConfigReaderTest {

    private static final String SH_BFD_TEMPLATE_OUTPUT = "bfd-template single-hop bfd1\n";

    private static final Config BFD_TEMPLATE_CONFIG_BUILDER = new ConfigBuilder()
            .setName("bfd1")
            .setType(Type.SingleHop)
            .build();

    @Test
    public void testBfdTemplateConfigReader() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setName("bfd1");
        BfdTemplateConfigReader.parseBfdTemplateConfig(SH_BFD_TEMPLATE_OUTPUT, configBuilder);
        Assert.assertEquals(BFD_TEMPLATE_CONFIG_BUILDER, configBuilder.build());
    }
}

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

package io.frinx.cli.unit.huawei.aaa.handler.radius;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.aaa.huawei.extension.rev210803.radius.extension.radius.TemplateKey;

public class RadiusTemplateReaderTest {

    private final String outputRadiusTemplates = "radius-server template default\n"
            + "radius-server template RADIUS-ZIGGO\n"
            + "radius-server template Test\n";

    @Test
    public void testRadiusTemplatesIds() {
        List<TemplateKey> keys = RadiusTemplateReader.getAllIds(outputRadiusTemplates);
        Assert.assertEquals(keys, Arrays.asList(new TemplateKey("default"),
                new TemplateKey("RADIUS-ZIGGO"), new TemplateKey("Test")));
    }
}
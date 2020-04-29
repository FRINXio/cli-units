/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.two.rate.three.color.ConfigBuilder;

public class ProfileThreeColorConfigReaderTest {

    private static final String OUTPUT =
        "traffic-profiling standard-profile create port 4 profile 1 name Prof_1 cir 10048 eir 0 cbs 128 ebs 0\n"
        + "traffic-profiling standard-profile create port 5 profile 1 name Test1 cir 10048 eir 0 cbs 128 ebs 0\n"
        + "traffic-profiling standard-profile create port 5 profile 2 name Test2 cir 20048 eir 0 cbs 128 ebs 0\n";

    @Test
    public void parseTwoRateThreeColorConfigTest() {
        ProfileThreeColorConfigReader reader = new ProfileThreeColorConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder configBuilder = new ConfigBuilder();

        reader.parseThreeColorConfig(OUTPUT, configBuilder, "Test1");
        Assert.assertEquals("10048", configBuilder.getCir().toString());

        reader.parseThreeColorConfig(OUTPUT, configBuilder, "Test2");
        Assert.assertEquals("20048", configBuilder.getCir().toString());
    }
}

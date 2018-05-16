/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosMaxQueueDepthMsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._1r2c.top.one.rate.two.color.ConfigBuilder;

public class OneRateTwoColorConfigReaderTest {

    private static final String OUTPUT ="Mon Mar 26 12:08:19.067 UTC\r\n" +
        " class map1\r\n" +
        "  set mpls experimental topmost 5\r\n" +
        "  police rate percent 33 \r\n" +
        "  ! \r\n" +
        "  queue-limit 4 ms \r\n" +
        "  bandwidth remaining percent 10 \r\n" +
        "  bandwidth percent 15 \r\n" +
        " ! \r\n" +
        " class class-default\r\n" +
        " end-policy-map\r\n" +
        "! \r\n";

    private static final String OUTPUT_DEFAULT =
        " class class-default\r\n" +
        "  set mpls experimental topmost 10\r\n" +
        "  priority level 2 \r\n" +
        "  queue-limit 3 ms \r\n" +
        "  bandwidth remaining percent 9 \r\n" +
        "  bandwidth percent 14 \r\n" +
        " ! \r\n" +
        " end-policy-map\r\n" +
        "! \r\n";

    @Test
    public void testOneRateTwoColorConfig() {
        ConfigBuilder builder = new ConfigBuilder();
        String finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT, "map1");
        OneRateTwoColorConfigReader.fillInConfig(finalOutput, builder);
        Assert.assertEquals(4, builder.getAugmentation(QosMaxQueueDepthMsAug.class).getMaxQueueDepthMs().intValue());
        Assert.assertEquals(10, builder.getCirPctRemaining().getValue().intValue());
        Assert.assertEquals(15, builder.getCirPct().getValue().intValue());

        ConfigBuilder builder1 = new ConfigBuilder();
        finalOutput = OneRateTwoColorConfigReader.limitOutput(OUTPUT_DEFAULT, "class-default");
        OneRateTwoColorConfigReader.fillInConfig(finalOutput, builder1);
        Assert.assertEquals(3, builder1.getAugmentation(QosMaxQueueDepthMsAug.class).getMaxQueueDepthMs().intValue());
        Assert.assertEquals(9, builder1.getCirPctRemaining().getValue().intValue());
        Assert.assertEquals(14, builder1.getCirPct().getValue().intValue());
    }
}

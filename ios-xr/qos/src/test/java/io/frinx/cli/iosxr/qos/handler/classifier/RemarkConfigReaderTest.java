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

package io.frinx.cli.iosxr.qos.handler.classifier;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosRemarkQosGroupAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.common.remark.actions.ConfigBuilder;

public class RemarkConfigReaderTest {

    private static String OUTPUT = "Wed Mar 14 12:43:30.768 UTC\n" +
            "  set precedence 4\n" +
            "  set mpls experimental topmost 1\n" +
            "  set qos-group 30 5-10\n";

    @Test
    public void testParseRemarks() {
        ConfigBuilder builder = new ConfigBuilder();
        RemarkConfigReader.parseRemarks(OUTPUT, builder);

        Assert.assertEquals(1, builder.getSetMplsTc().shortValue());
        Assert.assertEquals(2, builder.getAugmentation(QosRemarkQosGroupAug.class).getSetQosGroup().size());
        Assert.assertEquals(Long.valueOf(30), builder.getAugmentation(QosRemarkQosGroupAug.class).getSetQosGroup().get(0).getUint32());
        Assert.assertEquals("5..10", builder.getAugmentation(QosRemarkQosGroupAug.class).getSetQosGroup().get(1).getQosGroupRange().getValue());
    }
}

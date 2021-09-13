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
package io.frinx.cli.unit.huawei.qos.handler.classifier;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.DscpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.extension.rev180304.QosIpv4ConditionAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.classifier.terms.top.terms.term.ConditionsBuilder;

public class ConditionsReaderTest {

    private static String OUTPUT_ANY = "traffic classifier VOICE operator or\n"
            + " if-match dscp ef cs4\n";

    private ConditionsBuilder builder;

    private void setup(String output, String line) {
        this.builder = new ConditionsBuilder();
        ConditionsReader.filterParsing(output, line, builder);
    }

    @Test
    public void testAnyDscp() {
        setup(OUTPUT_ANY, "2");
        Assert.assertEquals(DscpBuilder.getDefaultInstance("cs4").getDscp(),
                builder.getIpv4().getConfig().getDscp());

        setup(OUTPUT_ANY, "1");
        QosIpv4ConditionAug v4aug = builder.getIpv4().getConfig().getAugmentation(QosIpv4ConditionAug.class);
        Assert.assertEquals(DscpBuilder.getDefaultInstance("ef").getDscpEnumeration(),
                v4aug.getDscpEnum());
    }
}

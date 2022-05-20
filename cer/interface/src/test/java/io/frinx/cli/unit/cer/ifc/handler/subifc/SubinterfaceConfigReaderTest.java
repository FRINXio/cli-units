/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler.subifc;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggSubifAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggSubifAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cer.rev220420.CerIfAggregateSubifExtension;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;

public class SubinterfaceConfigReaderTest {

    private static final String SH_INTERFACE_LINK_AGGREGATE_RUN = "interface link-aggregate 1.0\n"
            + " isis wide-metric 100 level-1\n"
            + " ip ospf 10 cost 10";

    private static final Config EXPECTED_CONFIG = new ConfigBuilder()
            .setName("link-aggregate 1.0")
            .setIndex(0L)
            .setEnabled(true)
            .addAugmentation(CerIfAggSubifAug.class, new CerIfAggSubifAugBuilder()
                    .setLevelType(CerIfAggregateSubifExtension.LevelType.LEVEL1)
                    .setMetric(100L)
                    .setCost(10)
                    .build())
            .build();

    @Test
    public void testParseLinkAggregate() {
        var configBuilder = new ConfigBuilder();
        SubinterfaceConfigReader.parseSubinterfaceConfig(SH_INTERFACE_LINK_AGGREGATE_RUN,
                configBuilder, 0L, "link-aggregate 1.0");
        Assert.assertEquals(EXPECTED_CONFIG, configBuilder.build());
    }
}

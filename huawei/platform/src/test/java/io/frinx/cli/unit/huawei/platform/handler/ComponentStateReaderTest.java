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

package io.frinx.cli.unit.huawei.platform.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.FAN;

public class ComponentStateReaderTest {

    public static final String OUTPUT_VERSION = "Huawei Versatile Routing Platform Software\n"
        + "VRP (R) software, Version 8.130 (S6800 V800R013C00SPC560B560)\n"
        + "Copyright (C) 2012-2016 Huawei Technologies Co., Ltd.\n"
        + "HUAWEI S6800 uptime is 0 day, 0 hour, 1 minute\n"
        + "SVRP Platform Version 1.0\n";

    @Test
    public void testParse() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "2102120866P0FC000104", ComponentReaderTest.OUTPUT);
        Assert.assertEquals(new StateBuilder().setId("2102120866P0FC000104")
                        .setDescription("Fan Box,NE5000E-X16A,CR56FCBJ,Fan Box,Fan Unit")
                        .setName("2102120866P0FC000104")
                        .setPartNo("02120866")
                        .setVersion("00")
                        .setSerialNo("2102120866P0FC000104")
                        .setType(new PlatformComponentState.Type(FAN.class))
                        .build(),

                stateBuilder.build());


        stateBuilder = new StateBuilder();
        ComponentStateReader.parseOSVersions(stateBuilder, OUTPUT_VERSION);
        Assert.assertEquals(new StateBuilder().setId("Versatile Routing Platform")
                        .setName("OS")
                        .setSoftwareVersion("8.130 (S6800 V800R013C00SPC560B560)")
                        .build(),

                stateBuilder.build());
    }
}
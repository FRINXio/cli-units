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

package io.frinx.cli.unit.ios.network.instance.handler.l2vsi.cp;

import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.Local;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.LocalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.EndpointBuilder;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class L2VSIConnectionPointsWriterTest implements CliFormatter {

    private String input;
    private String expOutput;

    public L2VSIConnectionPointsWriterTest(String input, String expOutput) {
        this.input = input;
        this.expOutput = expOutput;
    }

    static CliFormatter cliF = new CliFormatter() {};

    private static String WRITE_LOCAL_SINT = "conf t\n" +
            "interface ifc_test\n" +
            "service instance cpId_unknown ethernet\n" +
            "encapsulation dot1q Config [_interface=_2layer, augmentation=[]]\n" +
            "rewrite ingress tag pop 1 symmetric\n" +
            "bridge-domain 20\n" +
            "end";

    private static String WRITE_LOCAL = "conf t\n" +
            "interface ifc_test\n" +
            "service instance cpId_unknown ethernet\n" +
            "encapsulation untagged\n" +
            "bridge-domain 20\n" +
            "end";


    static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.Config config = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.ConfigBuilder()
            .setEndpointId("eid")
            .build();

    static Config config3layer = new ConfigBuilder()
            .setInterface("_2layer")
            .build();

    static Config config2layer = new ConfigBuilder()
            .setSubinterface(config3layer)
            .build();


    static Local localWriteLocalSINT = new LocalBuilder()
            .setConfig(config2layer)
            .build();

    static Local localWriteLocal = new LocalBuilder()
            .setConfig(config3layer)
            .build();

    static Endpoint endpointWriteLocalSINT = new EndpointBuilder()
            .setConfig(config)
            .setLocal(localWriteLocalSINT)
            .build();

    static Endpoint endpointWriteLocal = new EndpointBuilder()
            .setConfig(config)
            .setLocal(localWriteLocal)
            .build();


    static String cpId = "cpId_unknown";
    static int bdIndex = 20;
    static String ifc1 = "ifc_test";

    static String exp_write_local_sint = cliF.fT(L2VSIConnectionPointsWriter.WRITE_LOCAL,
            "ifc1", ifc1,
            "cpId", cpId,
            "subInterface", endpointWriteLocalSINT,
            "bdIndex", bdIndex);

    static String exp_write_local = cliF.fT(L2VSIConnectionPointsWriter.WRITE_LOCAL,
            "ifc1", ifc1,
            "cpId", cpId,
            "subInterface", endpointWriteLocal,
            "bdIndex", bdIndex);

    @Parameterized.Parameters(name = "statement test: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {exp_write_local_sint, WRITE_LOCAL_SINT},
                {exp_write_local, WRITE_LOCAL}
        });
    }


    @Test
    public void testWriteLocal() {

        Assert.assertEquals(input, expOutput);

    }
}
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

package io.frinx.cli.unit.brocade.network.instance.l2p2p.cp;


import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.Local;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.LocalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.remote.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.remote.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import io.frinx.cli.unit.utils.CliFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.RemoteBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ConnectionPointsWriterTest implements CliFormatter{

    private String input;
    private String output;

    public ConnectionPointsWriterTest(String input, String output) {
        this.input = input;
        this.output = output;
    }

    static CliFormatter cliF = new CliFormatter() {};

    static final String MOD_VLL = "conf t\n" +
            "router mpls\n" +
            "vll netName 125\n" +
            "vll-peer 125.125.25.5\n" +
            "vlan hoj\n" +
            "tag ifcType ifcNumber\n" +
            "end";

    static final String MOD_VLL_S = "conf t\n" +
            "router mpls\n" +
            "vll netName 125\n" +
            "vll-peer 125.125.25.5\n" +
            "untag ifcType ifcNumber\n" +
            "end";

    static final String MOD_VLL_D = "conf t\n" +
            "router mpls\n" +
            "no vll netName 125\n" +
            "end";

    static final String MOD_LOCAL_VLL = "conf t\n" +
            "router mpls\n" +
            "vll-local netName\n" +
            "vlan endpoint\n" +
            "tag ifcType ifcName\n" +
            "end";

    static final String MOD_LOCAL_WS = "conf t\n" +
            "router mpls\n" +
            "vll-local netName\n" +
            "untag ifcType ifcName\n" +
            "end";

    static final String MOD_LOCAL_VLL_D = "conf t\n" +
            "router mpls\n" +
            "no vll-local netName\n" +
            "end";

    static Config config = new ConfigBuilder()
            .setVirtualCircuitIdentifier(125l)
            .setRemoteSystem(new IpAddress(new Ipv4Address("125.125.25.5")))
            .build();

    static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.
            Config conf = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.ConfigBuilder()
                .setSubinterface("endpoint")
            .build();

    static Endpoint remote = new EndpointBuilder()
            .setRemote(new RemoteBuilder()
                    .setConfig(config)
                    .build())
            .setLocal(new LocalBuilder().setConfig(conf).build())
            .build();

    static Local local = new LocalBuilder()
            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local.ConfigBuilder()
                    .setSubinterface("hoj").build())
            .build();

    static String mod_vll = cliF.fT(ConnectionPointsWriter.MOD_VLL,
            "netName", "netName",
            "remote", remote,
            "local", local,
            "ifcType", "ifcType",
            "ifcNumber", "ifcNumber");

    static String mod_vll_s = cliF.fT(ConnectionPointsWriter.MOD_VLL,
            "netName", "netName",
            "remote", remote,
            "ifcType", "ifcType",
            "ifcNumber", "ifcNumber");

    static String mod_vll_d = cliF.fT(ConnectionPointsWriter.MOD_VLL,
            "netName", "netName",
            "delete", true,
            "remote", remote);

    static String mod_local_vll = cliF.fT(ConnectionPointsWriter.MOD_LOCAL_VLL,
            "netName", "netName",
            "endpoint", remote,
            "ifcType", "ifcType",
            "ifcName", "ifcName");

    static String mod_local_ws = cliF.fT(ConnectionPointsWriter.MOD_LOCAL_VLL,
            "netName", "netName",
            "ifcType", "ifcType",
            "ifcName", "ifcName");

    static String mod_local_vll_d = cliF.fT(ConnectionPointsWriter.MOD_LOCAL_VLL,
            "delete", true,
            "netName", "netName");

    @Parameterized.Parameters(name = "name: {index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {mod_vll, MOD_VLL},
                {mod_vll_s, MOD_VLL_S},
                {mod_vll_d, MOD_VLL_D},
                {mod_local_vll, MOD_LOCAL_VLL},
                {mod_local_ws, MOD_LOCAL_WS},
                {mod_local_vll_d, MOD_LOCAL_VLL_D}
        });
    }

    @Test
    public void testFirst() {
        Assert.assertEquals(input, output);
    }

}
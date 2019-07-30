/*
 * Copyright © 2019 Frinx and others.
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

import io.frinx.cli.unit.utils.CliFormatter;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.LocalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.RemoteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

@SuppressWarnings("Indentation")
@RunWith(Parameterized.class)
public class L2P2PPointsWriterTest implements CliFormatter {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        L2P2PPointsWriter.VLL_IFC,
                        getRemote(44L, "1.2.3.4"),
                        getLocal("ethernet 1/8", null),
                        "configure terminal\n"
                                + "router mpls\n"
                                + "vll abcd 44\n"
                                + "untag ethernet 1/8\n"
                                + "end\n",
                        null
                },
                {
                        L2P2PPointsWriter.VLL_SUBINTERFACE,
                        getRemote(44L, "1.2.3.4"),
                        getLocal("ethernet 1/8", 99L),
                        "configure terminal\n"
                                + "router mpls\n"
                                + "vll abcd 44\n"
                                + "vlan 99\n"
                                + "tag ethernet 1/8\n"
                                + "end\n",
                        null
                },
                {
                        L2P2PPointsWriter.DELETE_VLL,
                        getRemote(44L, "1.2.3.4"),
                        null,
                        "configure terminal\n"
                                + "router mpls\n"
                                + "no vll abcd 44\n"
                                + "end\n",
                        null
                },
                {
                        L2P2PPointsWriter.DELETE_VLL_LOCAL,
                        null,
                        null,
                        "configure terminal\n"
                                + "router mpls\n"
                                + "no vll-local abcd\n"
                                + "end\n",
                        null
                },
                {
                        L2P2PPointsWriter.VLL_REMOTE,
                        getRemote(44L, "1.2.3.4"),
                        getLocal("ethernet 1/8", null),
                        "configure terminal\n"
                                + "router mpls\n"
                                + "vll abcd 44\n"
                                + "vll-mtu 1234\n"
                                + "vll-peer 1.2.3.4\n"
                                + "end\n",
                        1234
                },
                {
                        L2P2PPointsWriter.VLL_LOCAL_IFC,
                        null,
                        getLocal("ethernet 1/8", null),
                        "configure terminal\n"
                                + "router mpls\n"
                                + "vll-local abcd\n"
                                + "untag ethernet 1/8\n"
                                + "end\n",
                        null
                },
                {
                        L2P2PPointsWriter.VLL_LOCAL_SUBINTERFACE,
                        null,
                        getLocal("ethernet 1/8", 99L),
                        "configure terminal\n"
                                + "router mpls\n"
                                + "vll-local abcd\n"
                                + "vlan 99\n"
                                + "tag ethernet 1/8\n"
                                + "end\n",
                        null
                }
        });
    }

    private static Endpoint getLocal(String ifcName, Long vlan) {
        return new EndpointBuilder()
                .setEndpointId("2")
                .setConfig(new ConfigBuilder()
                        .setType(LOCAL.class)
                        .build())
                .setLocal(new LocalBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                                .rev170228.network.instance.top.network.instances.network.instance.connection.points
                                .connection.point.endpoints.endpoint.local.ConfigBuilder()
                                .setInterface(ifcName)
                                .setSubinterface(vlan)
                                .build())
                        .build())
                .build();
    }

    private static Endpoint getRemote(long vcid, String remoteIp) {
        return new EndpointBuilder()
                .setEndpointId("1")
                .setConfig(new ConfigBuilder()
                        .setType(REMOTE.class)
                        .build())
                .setRemote(new RemoteBuilder()
                        .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance
                                .rev170228.network.instance.top.network.instances.network.instance.connection.points
                                .connection.point.endpoints.endpoint.remote.ConfigBuilder()
                                .setVirtualCircuitIdentifier(vcid)
                                .setRemoteSystem(new IpAddress(new Ipv4Address(remoteIp)))
                                .build())
                        .build())
                .build();
    }

    private static final String NETNAME = "abcd";
    private final String template;
    private final Endpoint remote;
    private final Endpoint local;
    private final String output;
    private final Integer mtu;

    public L2P2PPointsWriterTest(String template, Endpoint remote, Endpoint local, String output, Integer mtu) {
        this.template = template;
        this.remote = remote;
        this.local = local;
        this.output = output;
        this.mtu = mtu;
    }

    @Test
    public void testFormatting() {
        String formatted = fT(template, "network", NETNAME, "remote", remote, "local", local, "mtu", mtu);
        Assert.assertEquals(output, formatted);
    }
}
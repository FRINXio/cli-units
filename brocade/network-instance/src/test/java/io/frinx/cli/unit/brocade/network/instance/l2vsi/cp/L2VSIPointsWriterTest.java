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

package io.frinx.cli.unit.brocade.network.instance.l2vsi.cp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Lists;
import io.frinx.cli.unit.utils.CliFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.NiCpBrocadeAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.cp.extension.brocade.rev190812.NiCpBrocadeAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.LocalBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.RemoteBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.LOCAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;

public class L2VSIPointsWriterTest implements CliFormatter {

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {
                L2VSIPointsWriter.VPLS_IFC_TAG,
                Lists.newArrayList(getRemote(44L, "1.2.3.4")),
                getLocal("ethernet 1/8", 100L, false),
                """
                configure terminal
                router mpls
                vpls test 44
                vlan 100
                tag ethernet 1/8
                end
                    """
            },
            {
                L2VSIPointsWriter.VPLS_IFC_UNTAG,
                Lists.newArrayList(getRemote(44L, "1.2.3.4")),
                getLocal("ethernet 1/8", 99L, true),
                """
                configure terminal
                router mpls
                vpls test 44
                vlan 99
                untag ethernet 1/8
                end
                    """
            },
            {
                L2VSIPointsWriter.DELETE_VPLS,
                Lists.newArrayList(getRemote(44L, "1.2.3.4")),
                null,
                """
                configure terminal
                router mpls
                no vpls test 44
                end
                    """
            },
            {
                L2VSIPointsWriter.VPLS_REMOTE,
                Lists.newArrayList(getRemote(44L, "1.2.3.4"),
                    getRemote(44L, "1.2.3.5"),
                    getRemote(44L, "1.2.3.6")),
                getLocal("ethernet 1/8", null, false),
                """
                configure terminal
                router mpls
                vpls test 44
                vpls-peer 1.2.3.4
                vpls-peer 1.2.3.5
                vpls-peer 1.2.3.6
                end
                    """
            }
        });
    }

    private static Endpoint getLocal(String ifcName, Long vlan, boolean untagged) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top
                .network.instances.network.instance.connection.points.connection.point.endpoints.endpoint.local
                .ConfigBuilder configBuilder = new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network
                .instance.rev170228.network.instance.top.network.instances.network.instance.connection.points
                .connection.point.endpoints.endpoint.local.ConfigBuilder()
                .setInterface(ifcName)
                .setSubinterface(vlan);
        if (untagged) {
            configBuilder.addAugmentation(NiCpBrocadeAug.class, new NiCpBrocadeAugBuilder()
                    .setSubinterfaceUntagged(untagged)
                    .build());
        }
        return new EndpointBuilder()
                .setEndpointId("2")
                .setConfig(new ConfigBuilder()
                        .setType(LOCAL.class)
                        .build())
                .setLocal(new LocalBuilder()
                        .setConfig(configBuilder
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

    private static final String NETNAME = "test";

    @MethodSource("data")
    @ParameterizedTest
    void testFormatting(String template, List<Endpoint> remote, Endpoint local, String output) {
        Long vccid = remote.get(0).getRemote().getConfig().getVirtualCircuitIdentifier();
        if (L2VSIPointsWriter.DELETE_VPLS.equals(template)) {
            String formatted = fT(template, "network", NETNAME, "remote", remote.get(0));
            assertEquals(output, formatted);
        } else {
            String remoteVar = L2VSIPointsWriter.VPLS_REMOTE.equalsIgnoreCase(template) ? "remotes" : "remote";
            String formatted = fT(template, "network", NETNAME, remoteVar, remote, "local", local, "vccid", vccid);
            assertEquals(output, formatted);
        }
    }

}
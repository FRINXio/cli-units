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

package io.frinx.cli.unit.junos.ospf.handler;

import static org.hamcrest.MatcherAssert.assertThat;

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Timers;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class AreaInterfaceTimersConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;
    private AreaInterfaceTimersConfigReader target;
    private static final String OUTPUT_TIERS =
            "set protocols ospf area 10.1.2.12 interface 235.0.0.0 retransmit-interval 151\r\n";
    private static final String SH_RUN_OSPF = "show configuration protocols ospf area";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new AreaInterfaceTimersConfigReader(cli));
    }

    @Test
    void testReadCurrentAttributesIsTarget() throws Exception {
        final String vrfName = "APTN";
        final String areaName = "10.1.2.12";
        final String ifName = "235.0.0.0";
        final Long TimersName = 151L;
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
                .child(Ospfv2.class)
                .child(Areas.class)
                .child(Area.class, new AreaKey(new OspfAreaIdentifier(new DottedQuad(areaName))))
                .child(Interfaces.class)
                .child(Interface.class, new InterfaceKey(ifName))
                .child(Timers.class)
                .child(Config.class);

        Mockito.doReturn(OUTPUT_TIERS).when(target).blockingRead(
            Mockito.eq(String.format(AreaInterfaceTimersConfigReader.SHOW_OSPF_INT, areaName, ifName)),
            Mockito.eq(cli),
            Mockito.eq(iid),
            Mockito.eq(readContext));
        Mockito.doReturn(OUTPUT_TIERS).when(target).blockingRead(
                Mockito.eq(SH_RUN_OSPF),
                Mockito.eq(cli),
                Mockito.eq(iid),
                Mockito.eq(readContext));

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(iid, builder, readContext);

        assertThat(builder.getRetransmissionInterval(), CoreMatchers.equalTo(TimersName));
    }
}

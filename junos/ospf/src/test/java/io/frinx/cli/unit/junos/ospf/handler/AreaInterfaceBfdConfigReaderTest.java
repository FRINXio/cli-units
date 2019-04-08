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

import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.ext.rev180211.OspfAreaIfBfdConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.Bfd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceBfdConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private AreaInterfaceBfdConfigReader target;

    private static final String OUTPUT_AREAS = "set routing-instances APTN instance-type virtual-router\r\n"
            + "set routing-instances APTN interface xe-0/0/34.0\r\n"
            + "set routing-instances APTN interface xe-0/0/35.0\r\n"
            + "set routing-instances APTN interface xe-0/0/36.0\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0 disable\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0 interface-type p2p\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0 metric 500\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0 priority 1\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
            + " bfd-liveness-detection minimum-interval 150\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
            + " bfd-liveness-detection minimum-receive-interval 15\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
            + " bfd-liveness-detection multiplier 3\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.22 interface xe-0/0/35.0 disable\r\n"
            + "set routing-instances APTN protocols ospf area 10.51.246.23 interface xe-0/0/36.0 disable\r\n";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new AreaInterfaceBfdConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributesIsTarget() throws Exception {
        final String vrfName = "APTN";
        final String areaName = "10.51.246.21";
        final String ifName = "xe-0/0/34.0";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
                .child(Ospfv2.class)
                .child(Areas.class)
                .child(Area.class, new AreaKey(new OspfAreaIdentifier(new DottedQuad(areaName))))
                .child(Interfaces.class)
                .child(Interface.class, new InterfaceKey(ifName))
                .augmentation(OspfAreaIfBfdConfAug.class)
                .child(Bfd.class)
                .child(Config.class);

        Mockito.doReturn(OUTPUT_AREAS).when(target).blockingRead(
            Mockito.eq(String.format(AreaInterfaceBfdConfigReader.SHOW_OSPF_INT,
                    String.format(" routing-instances %s", vrfName), areaName, ifName)),
            Mockito.eq(cli),
            Mockito.eq(iid),
            Mockito.eq(readContext));

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributes(iid, builder, readContext);

        Assert.assertThat(builder.getMinInterval(), CoreMatchers.equalTo(150L));
        Assert.assertThat(builder.getMinReceiveInterval(), CoreMatchers.equalTo(15L));
        Assert.assertThat(builder.getMultiplier(), CoreMatchers.equalTo(3L));
    }
}

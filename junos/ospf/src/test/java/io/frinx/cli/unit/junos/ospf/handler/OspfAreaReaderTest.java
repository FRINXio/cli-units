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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.Ospfv2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.Areas;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.AreaKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfAreaReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private OspfAreaReader target;

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
        + " bfd-liveness-detection minimum-receive-interval 150\r\n"
        + "set routing-instances APTN protocols ospf area 10.51.246.21 interface xe-0/0/34.0"
        + " bfd-liveness-detection multiplier 3\r\n"
        + "set routing-instances APTN protocols ospf area 10.51.246.22 interface xe-0/0/35.0 disable\r\n"
        + "set routing-instances APTN protocols ospf area 10.51.246.23 interface xe-0/0/36.0 disable\r\n";

    private static final List<AreaKey> EXPECTED_AREA_KEYS = Lists.newArrayList(
            new AreaKey(new OspfAreaIdentifier(new DottedQuad("10.51.246.21"))),
            new AreaKey(new OspfAreaIdentifier(new DottedQuad("10.51.246.22"))),
            new AreaKey(new OspfAreaIdentifier(new DottedQuad("10.51.246.23"))));

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new OspfAreaReader(cli));
    }

    @Test
    public void testGetAllIds_001() throws Exception {
        final InstanceIdentifier<Area> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey("APTN"))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
                .child(Ospfv2.class)
                .child(Areas.class)
                .child(Area.class);

        Mockito.doReturn(OUTPUT_AREAS).when(target).blockingRead(
                Mockito.eq(String.format(OspfAreaReader.SH_RUN_OSPF_AREA, " routing-instances APTN")),
                Mockito.eq(cli),
                Mockito.eq(iid),
                Mockito.eq(readContext));

        List<AreaKey> result = target.getAllIds(iid, readContext);

        Assert.assertThat(result, CoreMatchers.equalTo(EXPECTED_AREA_KEYS));

        Mockito.verify(target, Mockito.times(1)).blockingRead(
            String.format(OspfAreaReader.SH_RUN_OSPF_AREA, " routing-instances APTN"),
            cli,
            iid,
            readContext);
    }

    @Test
    public void testReadCurrentAttributesForType() throws Exception {
        final String area = "10.51.246.21";
        final InstanceIdentifier<Area> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey("APTN"))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
                .child(Ospfv2.class)
                .child(Areas.class)
                .child(Area.class, new AreaKey(new OspfAreaIdentifier(new DottedQuad(area))));

        final AreaBuilder builder = new AreaBuilder();

        target.readCurrentAttributes(iid, builder , readContext);

        Assert.assertEquals(builder.getIdentifier().getDottedQuad().getValue(), area);
    }
}

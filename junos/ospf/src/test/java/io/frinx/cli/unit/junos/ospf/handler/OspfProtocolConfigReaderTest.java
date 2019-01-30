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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolConfigReaderTest {
    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private OspfProtocolConfigReader target;

    private static final String OUTPUT =
            "set policy-options policy-statement OUT-FIL term 1 from protocol direct\r\n"
            + "set policy-options policy-statement OUT-FIL term 1 then metric 500\r\n"
            + "set policy-options policy-statement OUT-FIL term 1 then accept\r\n"
            + "set routing-instances APTN instance-type virtual-router\r\n"
            + "set routing-instances APTN protocols ospf export OUT-FIL\r\n"
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new OspfProtocolConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributesIsTarget() throws Exception {
        final String vrfName = "APTN";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
                .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
                .child(Protocols.class)
                .child(Protocol.class, new ProtocolKey(OSPF.class, OspfProtocolReader.OSPF_NAME))
                .child(Config.class);

        Mockito.doReturn(OUTPUT).when(target).blockingRead(
                Mockito.eq(String.format(OspfProtocolConfigReader.SHOW_EXPORT_POLICY, " routing-instances APTN")),
                Mockito.eq(cli),
                Mockito.eq(iid),
                Mockito.eq(readContext));

        final ConfigBuilder builder = new ConfigBuilder();

        target.readCurrentAttributesForType(iid, builder, readContext);

        Assert.assertThat(builder.getAugmentation(ProtocolConfAug.class).getExportPolicy(),
                CoreMatchers.equalTo("OUT-FIL"));
    }
}

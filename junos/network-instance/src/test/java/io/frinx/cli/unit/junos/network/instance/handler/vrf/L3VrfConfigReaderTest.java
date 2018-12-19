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

package io.frinx.cli.unit.junos.network.instance.handler.vrf;

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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigReaderTest {
    private static final String OUTPUT_VRFS = "set routing-instance router-A instance-type virtual-router\r\n"
        + "set routing-instance router-B\r\n"
        + "set routing-instance router-C instance-type virtual-router\r\n"
        + "set routing-instance router-D instance-type unknown-router-type";

    @Mock
    private Cli cli;
    @Mock
    private ReadContext readContext;

    private L3VrfConfigReader target;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        target = Mockito.spy(new L3VrfConfigReader(cli));
    }

    @Test
    public void testReadCurrentAttributesIsTarget() throws Exception {
        final String vrfName = "VRF-001";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
            .child(Config.class);

        final ConfigBuilder builder = new ConfigBuilder();

        Mockito.doReturn(Boolean.TRUE).when(target).isL3Vrf(vrfName, iid, readContext);

        target.readCurrentAttributes(iid, builder, readContext);

        Assert.assertThat(builder.getName(), CoreMatchers.sameInstance(vrfName));
        Assert.assertThat(builder.getType(), CoreMatchers.sameInstance(L3VRF.class));

        Mockito.verify(target, Mockito.times(1)).isL3Vrf(vrfName, iid, readContext);
    }

    @Test
    public void testReadCurrentAttributesIsNotTarget() throws Exception {
        final String vrfName = "VRF-001";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
            .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();
        final Config expectedData = new ConfigBuilder().build();

        Mockito.doReturn(Boolean.FALSE).when(target).isL3Vrf(vrfName, iid, readContext);

        target.readCurrentAttributes(iid, builder, readContext);

        Assert.assertThat(builder.build(), CoreMatchers.equalTo(expectedData));
        Mockito.verify(target, Mockito.times(1)).isL3Vrf(vrfName, iid, readContext);
    }

    @Test
    public void testIsL3Vrf() throws Exception {
        final String vrfName = "router-C";
        final InstanceIdentifier<Config> iid = IIDs.NETWORKINSTANCES
            .child(NetworkInstance.class, new NetworkInstanceKey(vrfName))
            .child(Config.class);
        final ConfigBuilder builder = new ConfigBuilder();
        final Config expectedData = new ConfigBuilder().build();

        Mockito.doReturn(OUTPUT_VRFS).when(target)
                .blockingRead(
                    Mockito.eq(L3VrfReader.DISPLAY_CONF_VRF),
                    Mockito.eq(cli),
                    Mockito.eq(iid),
                    Mockito.eq(readContext));

        target.isL3Vrf(vrfName, iid, readContext);

        Assert.assertThat(builder.build(), CoreMatchers.equalTo(expectedData));

        Mockito.verify(target, Mockito.times(1)).blockingRead(
            Mockito.eq(L3VrfReader.DISPLAY_CONF_VRF),
            Mockito.eq(cli),
            Mockito.eq(iid),
            Mockito.eq(readContext));
    }
}

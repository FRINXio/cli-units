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

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ni.base.handler.vrf.AbstractL3VrfReader;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfReaderTest {

    @Mock
    private Cli cli;

    @Mock
    private ReadContext readContext;

    private L3VrfReader reader;

    @Mock
    private AbstractL3VrfReader parentReader;

    private static final String OUTPUT_VRFS = "set routing-instances router-A instance-type virtual-router\r\n"
        + "set routing-instances router-B\r\n"
        + "set routing-instances router-C instance-type virtual-router\r\n"
        + "set routing-instances router-D instance-type unknown-router-type";

    private static final List<NetworkInstanceKey> EXPECTED_VRF_KEYS = Lists.newArrayList(
            new NetworkInstanceKey("router-A"),
            new NetworkInstanceKey("router-C"));

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reader = new L3VrfReader(cli);
    }

    @Test
    public void testGetAllIds() throws Exception {
        final InstanceIdentifier<NetworkInstance> iid = IIDs.NE_NETWORKINSTANCE;

        Mockito.doReturn(OUTPUT_VRFS).when(parentReader)
                .blockingRead(
                    Mockito.eq(L3VrfReader.DISPLAY_CONF_VRF),
                    Mockito.eq(cli),
                    Mockito.eq(iid),
                    Mockito.eq(readContext));

        List<NetworkInstanceKey> result = reader.getAllIds(parentReader, iid, readContext);

        Assert.assertEquals(EXPECTED_VRF_KEYS, result);
    }
}

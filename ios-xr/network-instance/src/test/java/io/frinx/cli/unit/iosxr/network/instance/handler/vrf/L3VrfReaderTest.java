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

package io.frinx.cli.unit.iosxr.network.instance.handler.vrf;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
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
    private static final String OUTPUT = " vrf iups\r\n vrf iups\r\n vrf icps\r\n vrf iups";

    private static final List<NetworkInstanceKey> EXPECTED_NWI_KEYS = Lists.newArrayList(
            new NetworkInstanceKey("iups"),
            new NetworkInstanceKey("icps")
            );

    @Mock
    CliReader parentReader;

    @Before
    public void setUp() throws ReadFailedException {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(OUTPUT).when(parentReader).blockingRead(Mockito.anyString(),
                Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testGetAllIds() throws Exception {
        final InstanceIdentifier<NetworkInstance> iid = InstanceIdentifier.create(NetworkInstance.class);
        L3VrfReader reader = new L3VrfReader(Mockito.mock(Cli.class));

        List<NetworkInstanceKey> lst = reader.getAllIds(parentReader, iid, Mockito.mock(ReadContext.class));
        Assert.assertEquals(lst, EXPECTED_NWI_KEYS);
    }
}

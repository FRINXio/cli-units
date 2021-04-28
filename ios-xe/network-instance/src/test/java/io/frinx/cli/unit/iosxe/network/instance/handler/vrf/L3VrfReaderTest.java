/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.network.instance.handler.vrf;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class L3VrfReaderTest {

    private static final String SH_IP_VRF = "ip vrf DEP_1  \n"
            + "ip vrf DEP_2  dfs dsf dsf\n"
            + "ip vrf a\n";

    private static final List<NetworkInstanceKey> IDS_EXPECTED =
            Lists.newArrayList("DEP_1", "DEP_2", "a", "default")
                    .stream()
                    .map(NetworkInstanceKey::new)
                    .collect(Collectors.toList());

    @Mock
    CliReader reader;

    @Mock
    InstanceIdentifier<?> id;

    @Mock
    ReadContext ctx;

    @Before
    public void setUp() throws ReadFailedException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(reader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.eq(id), Mockito.any(ReadContext.class))).thenReturn(SH_IP_VRF);
    }

    @Test
    public void testReader() throws ReadFailedException {
        Assert.assertEquals(IDS_EXPECTED, new L3VrfReader(Mockito.mock(Cli.class)).getAllIds(reader, id, ctx));
    }

}
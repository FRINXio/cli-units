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

package io.frinx.cli.unit.saos.network.instance.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.write.WriteContext;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L2VSI;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.saos.rev200211.L2VSICP;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class NetworkInstanceConfigWriterTest {

    @Mock
    Cli cli;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private static final InstanceIdentifier<Config> ID1 =
            IidUtils.createIid(IIDs.NE_NE_CONFIG, new NetworkInstanceKey("1"));
    private static final InstanceIdentifier<Config> ID2 =
            IidUtils.createIid(IIDs.NE_NE_CONFIG, new NetworkInstanceKey("2"));
    private static final InstanceIdentifier<Config> ID3 =
            IidUtils.createIid(IIDs.NE_NE_CONFIG, new NetworkInstanceKey("3"));

    private static final Config CONFIG1 = new ConfigBuilder()
            .setName("1")
            .setType(L2VSICP.class)
            .build();
    private static final Config CONFIG2 = new ConfigBuilder()
            .setName("2")
            .setType(L2VSICP.class)
            .build();
    private static final Config CONFIG3 = new ConfigBuilder()
            .setName("3")
            .setType(L2VSI.class)
            .build();

    @Test
    void writeNetworkInstance() throws Exception {
        final NetworkInstanceConfigWriter networkInstanceConfigWriter = new NetworkInstanceConfigWriter(cli);
        WriteContext mock = Mockito.mock(WriteContext.class);
        ModificationCache cache = new ModificationCache();
        cache.put(IIDs.NE_NE_CONFIG + "_updatesCount", 4);
        Mockito.when(mock.getModificationCache()).thenReturn(cache);

        networkInstanceConfigWriter.writeCurrentAttributes(ID1, CONFIG1, mock);
        networkInstanceConfigWriter.writeCurrentAttributes(ID1, CONFIG2, mock);
        networkInstanceConfigWriter.writeCurrentAttributes(ID3, CONFIG3, mock);
        assertEquals(2,
                NetworkInstanceConfigWriter.getUpdatesCache(mock, "updateOrCreate").values().size());

        List<Entry<InstanceIdentifier<Config>, Object>> sortEntries = NetworkInstanceConfigWriter
                .sortEntries(NetworkInstanceConfigWriter.getUpdatesCache(mock, "updateOrCreate"),
                        Comparator.reverseOrder());
        assertEquals("L2VSICP", ((Config) sortEntries.get(0).getValue()).getType().getSimpleName());
    }

    @Test
    void updateNetworkInstance() throws Exception {
        final NetworkInstanceConfigWriter networkInstanceConfigWriter = new NetworkInstanceConfigWriter(cli);
        WriteContext mock = Mockito.mock(WriteContext.class);
        ModificationCache cache = new ModificationCache();
        cache.put(IIDs.NE_NE_CONFIG + "_updatesCount", 3);
        Mockito.when(mock.getModificationCache()).thenReturn(cache);

        networkInstanceConfigWriter.updateCurrentAttributes(ID2, CONFIG1, CONFIG2, mock);
        networkInstanceConfigWriter.updateCurrentAttributes(ID1, CONFIG2, CONFIG1, mock);
        assertEquals(2,
                NetworkInstanceConfigWriter.getUpdatesCache(mock, "updateOrCreate").values().size());
    }

    @Test
    void deleteNetworkInstance() throws Exception {
        final NetworkInstanceConfigWriter networkInstanceConfigWriter = new NetworkInstanceConfigWriter(cli);
        WriteContext mock = Mockito.mock(WriteContext.class);
        ModificationCache cache = new ModificationCache();
        cache.put(IIDs.NE_NE_CONFIG + "_updatesCount", 3);
        Mockito.when(mock.getModificationCache()).thenReturn(cache);

        networkInstanceConfigWriter.deleteCurrentAttributes(ID2, CONFIG1, mock);
        networkInstanceConfigWriter.deleteCurrentAttributes(ID3, CONFIG3, mock);
        assertEquals(2,
                NetworkInstanceConfigWriter.getUpdatesCache(mock, "delete").values().size());

        List<Entry<InstanceIdentifier<Config>, Object>> sortEntries = NetworkInstanceConfigWriter
                .sortEntries(NetworkInstanceConfigWriter.getUpdatesCache(mock, "delete"),
                        Comparator.naturalOrder());
        assertEquals("L2VSI", ((Config) sortEntries.get(0).getValue()).getType().getSimpleName());
    }
}
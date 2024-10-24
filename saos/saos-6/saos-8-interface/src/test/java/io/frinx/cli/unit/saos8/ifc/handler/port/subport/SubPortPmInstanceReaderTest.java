/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.ModificationCache;
import io.fd.honeycomb.translate.read.ReadContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceKey;

class SubPortPmInstanceReaderTest {

    static final String OUTPUT = """
            pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance LAG=LP01_FRINX001_2500_1 profile-type BasicTxRx \
            bin-count 1
            pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_1 profile-type BasicTxRx
            pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_2 profile-type BasicTxRx start-time \
            20:00:00 start-date 2020-05-15 bin-count 11 alignment start-time
            pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_3 profile-type BasicTxRx bin-count 1
            pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_4 profile-type BasicTxRx bin-count 1
            """;

    private ReadContext context;
    private ModificationCache cache;

    @BeforeEach
    void setUp() {
        context = Mockito.mock(ReadContext.class);
        cache = Mockito.mock(ModificationCache.class);
        Mockito.when(context.getModificationCache()).thenReturn(cache);
        Mockito.when(cache.put(Mockito.anyString(), Mockito.anySet())).thenReturn(List.of());
    }

    @Test
    void getAllIdsTest() {
        var expected = List.of(new PmInstanceKey("PM_TEST_3"), new PmInstanceKey("PM_TEST_2"),
                new PmInstanceKey("PM_TEST_1"), new PmInstanceKey("LAG=LP01_FRINX001_2500_1"),
                new PmInstanceKey("PM_TEST_4"));
        assertEquals(expected, SubPortPmInstanceReader.getAllIds(OUTPUT, "LAG=LP01_FRINX001_2500_1",
                context));
    }
}

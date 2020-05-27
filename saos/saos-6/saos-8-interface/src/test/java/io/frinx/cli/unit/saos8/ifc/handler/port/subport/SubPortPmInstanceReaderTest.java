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

import io.frinx.cli.io.Cli;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstanceKey;

public class SubPortPmInstanceReaderTest {

    static final String OUTPUT =
            "pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance LAG=LP01_FRINX001_2500_1 profile-type "
            + "BasicTxRx bin-count 1\n"
            + "pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_1 profile-type "
            + "BasicTxRx\n"
            + "pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_2 profile-type "
            + "BasicTxRx start-time 20:00:00 start-date 2020-05-15 bin-count 11 alignment start-time\n"
            + "pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_3 profile-type "
            + "BasicTxRx bin-count 1\n"
            + "pm create sub-port LAG=LP01_FRINX001_2500_1 pm-instance PM_TEST_4 profile-type "
            + "BasicTxRx bin-count 1\n";

    @Test
    public void getAllIdsTest() {
        SubPortPmInstanceReader reader = new SubPortPmInstanceReader(Mockito.mock(Cli.class));

        List<PmInstanceKey> expected = Stream.of("LAG=LP01_FRINX001_2500_1", "PM_TEST_1",
                "PM_TEST_2", "PM_TEST_3", "PM_TEST_4")
                .map(PmInstanceKey::new)
                .collect(Collectors.toList());

        Assert.assertEquals(expected, SubPortPmInstanceReader.getAllIds(OUTPUT, "LAG=LP01_FRINX001_2500_1"));
    }
}

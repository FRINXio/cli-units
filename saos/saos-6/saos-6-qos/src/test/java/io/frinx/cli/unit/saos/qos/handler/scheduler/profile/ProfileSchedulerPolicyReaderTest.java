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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;

class ProfileSchedulerPolicyReaderTest {

    private static final String OUTPUT = """
            traffic-profiling set meter-provisioning eir
            traffic-profiling set port 1 mode advanced
            traffic-profiling set port 2 mode advanced
            traffic-profiling set port 6 mode hierarchical-vlan
            traffic-profiling standard-profile create port 1 profile 1 name CIA_CoS0 cir 50048 eir 0 cbs 8 ebs 0
            traffic-profiling standard-profile set port 1 profile CIA_CoS0
            traffic-profiling standard-profile create port 1 profile 2 name V4096 cir 10048 eir 0 cbs 256 ebs
            traffic-profiling standard-profile create port 5 profile 1 name Test1 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling standard-profile create port 5 profile 2 name Test2 cir 10048 eir 0 cbs 128 ebs 0
            traffic-profiling enable port 2
            traffic-profiling enable
            traffic-services queuing queue-map create rcos-map NNI-NNI
            traffic-services queuing queue-map set rcos-map NNI-NNI rcos 1 queue 1
            log flash add filter default traffic-profiling-mgr info
            traffic-services queuing egress-port-queue-group set queue 3 port 4 scheduler-weight 6
            traffic-services queuing egress-port-queue-group set port 7 cir 101056
            traffic-services queuing egress-port-queue-group set queue 0 port 7 scheduler-weight 6
            traffic-profiling set port 1 mode advanced
            """;

    @Test
    void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead((String) Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any()))
                .thenReturn(OUTPUT);

        List<SchedulerPolicyKey> ids = Arrays.asList(
                new SchedulerPolicyKey("CIA_CoS0"),
                new SchedulerPolicyKey("V4096"),
                new SchedulerPolicyKey("Test1"),
                new SchedulerPolicyKey("Test2"));

        assertEquals(ids, ProfileSchedulerPolicyReader.getAllIds(null, cliReader, null, null));
    }
}

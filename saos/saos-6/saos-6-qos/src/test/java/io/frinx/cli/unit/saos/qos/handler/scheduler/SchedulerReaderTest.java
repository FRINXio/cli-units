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

package io.frinx.cli.unit.saos.qos.handler.scheduler;

import io.frinx.cli.io.Cli;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;

public class SchedulerReaderTest {

    private static final String OUTPUT = "traffic-services queuing queue-map create rcos-map NNI-NNI\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 1 queue 1\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 2 queue 2\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 3 queue 3\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 4 queue 4\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 5 queue 5\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 6 queue 6\n"
            + "traffic-services queuing queue-map set rcos-map NNI-NNI rcos 7 queue 7\n"
            + "traffic-services queuing egress-port-queue-group set queue 3 port 4 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set port 7 cir 101056\n"
            + "traffic-services queuing egress-port-queue-group set queue 0 port 7 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set queue 1 port 7 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set queue 2 port 7 scheduler-weight 67\n"
            + "traffic-services queuing egress-port-queue-group set queue 3 port 7 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set queue 4 port 5 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set queue 6 port 6 scheduler-weight 6\n"
            + "traffic-services queuing egress-port-queue-group set queue 7 port 7 scheduler-weight 3\n";

    @Test
    public void getAllIdsTest() {
        SchedulerReader reader = new SchedulerReader(Mockito.mock(Cli.class));
        List<SchedulerKey> expected = Arrays.asList(new SchedulerKey(Long.parseLong("0")),
                new SchedulerKey(Long.parseLong("1")),
                new SchedulerKey(Long.parseLong("2")),
                new SchedulerKey(Long.parseLong("3")),
                new SchedulerKey(Long.parseLong("7")));
        List<SchedulerKey> allIds = reader.getAllIds(OUTPUT, "7");
        Assert.assertEquals(expected, allIds);
    }
}

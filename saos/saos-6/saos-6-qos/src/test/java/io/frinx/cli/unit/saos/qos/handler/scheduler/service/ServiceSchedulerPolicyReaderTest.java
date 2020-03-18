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

package io.frinx.cli.unit.saos.qos.handler.scheduler.service;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliReader;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ServiceSchedulerPolicyReaderTest {

    private static final String OUTPUT =
           "traffic-services queuing egress-port-queue-group set queue 3 port 4 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set port 7 cir 101056\n"
           + "traffic-services queuing egress-port-queue-group set queue 0 port 7 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set queue 1 port 7 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set queue 2 port 7 scheduler-weight 67\n"
           + "traffic-services queuing egress-port-queue-group set queue 3 port 7 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set queue 4 port 5 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set queue 6 port 6 scheduler-weight 6\n"
           + "traffic-services queuing egress-port-queue-group set queue 7 port 7 scheduler-weight 3\n"
           + "traffic-profiling set port 1 mode advanced\n";

    @Test
    public void getAllIdsTest() throws ReadFailedException {
        CliReader cliReader = Mockito.mock(CliReader.class);

        Mockito.when(cliReader.blockingRead(Mockito.anyString(), Mockito.any(Cli.class),
                Mockito.any(InstanceIdentifier.class), Mockito.any(ReadContext.class)))
                .thenReturn(OUTPUT);

        List<SchedulerPolicyKey> ids = Arrays.asList(
                new SchedulerPolicyKey("4"),
                new SchedulerPolicyKey("7"),
                new SchedulerPolicyKey("5"),
                new SchedulerPolicyKey("6"));

        Assert.assertEquals(ids, ServiceSchedulerPolicyReader.getAllIds(null, cliReader, null, null));
    }
}

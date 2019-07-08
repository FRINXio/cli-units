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

package io.frinx.cli.unit.iosxr.qos.handler.scheduler;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerKey;

public class SchedulerReaderTest {

    private static String OUTPUT = "policy-map plmap\r\n"
            + " class map1\r\n"
            + "  set mpls experimental topmost 5\r\n"
            + "  priority level 1 \r\n"
            + " ! \r\n"
            + " class class-default\r\n"
            + "  priority level 2 \r\n"
            + " ! \r\n"
            + " end-policy-map\r\n"
            + "! \r\n";

    @Test
    public void testSequenceIds() {
        List<SchedulerKey> keys = SchedulerReader.getSequenceIds(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList(1L, 2L),
            keys.stream().map(SchedulerKey::getSequence).collect(Collectors.toList()));
    }
}

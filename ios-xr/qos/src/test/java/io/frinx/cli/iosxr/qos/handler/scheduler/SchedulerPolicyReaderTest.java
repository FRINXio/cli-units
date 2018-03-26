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

package io.frinx.cli.iosxr.qos.handler.scheduler;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.qos.handler.scheduler.SchedulerPolicyReader;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyKey;

public class SchedulerPolicyReaderTest {

    private static String OUTPUT = "Thu Mar 22 13:33:38.062 UTC\n" +
        "policy-map plmap\n" +
        "policy-map plmap1\n" +
        "policy-map plmap2\n";

    @Test
    public void testGetIds() {
        List<SchedulerPolicyKey> keys = SchedulerPolicyReader.getSchedulerKeys(OUTPUT);
        Assert.assertFalse(keys.isEmpty());
        Assert.assertEquals(Lists.newArrayList("plmap", "plmap1", "plmap2"),
            keys.stream().map(SchedulerPolicyKey::getName).collect(Collectors.toList()));
    }
}

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

package io.frinx.cli.unit.saos.l2.cft.handler.profile;

import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.l2.cft.rev200416.l2.cft.top.l2.cft.profiles.ProfileKey;

public class L2CftProfileReaderTest {

    private static final String OUTPUT = "l2-cft create profile L2Test2 tunnel-method l2pt\n"
            + "l2-cft create profile CTB\n"
            + "l2-cft create profile VS11\n"
            + "l2-cft create profile MY\n"
            + "l2-cft create profile CTA\n"
            + "l2-cft create profile DAAN-1\n"
            + "l2-cft create profile VLAN111222\n";

    @Test
    public void getAllIdsTest() {
        List<ProfileKey> expected = Arrays.asList(new ProfileKey("L2Test2"),
                new ProfileKey("CTB"),
                new ProfileKey("VS11"),
                new ProfileKey("MY"),
                new ProfileKey("CTA"),
                new ProfileKey("DAAN-1"),
                new ProfileKey("VLAN111222"));

        Assert.assertEquals(expected, L2CftProfileReader.getAllIds(OUTPUT));
    }
}

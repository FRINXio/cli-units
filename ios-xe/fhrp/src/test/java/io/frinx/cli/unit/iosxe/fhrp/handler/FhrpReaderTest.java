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

package io.frinx.cli.unit.iosxe.fhrp.handler;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.Fhrp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.FhrpBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.Version.Vrrp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.fhrp.rev210512.fhrp.top.fhrp.VersionBuilder;


public class FhrpReaderTest {

    private static final String SH_FHRP_RUN = "fhrp version vrrp v3";

    private static final Fhrp FHRP = new FhrpBuilder()
            .setVersion(new VersionBuilder()
                    .setVrrp(Vrrp.V3)
                    .build())
            .build();

    @Test
    public void testParseFhrp() {
        FhrpBuilder fhrpBuilder = new FhrpBuilder();
        FhrpReader.parseFhrp(SH_FHRP_RUN, fhrpBuilder);
        Assert.assertEquals(FHRP, fhrpBuilder.build());
    }
}

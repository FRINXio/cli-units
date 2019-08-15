/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.brocade.ifc.handler;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1Builder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X88A8;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9100;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X9200;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPIDTYPES;

@RunWith(Parameterized.class)
public class TpIdInterfaceWriterTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {TPID0X8100.class, "8100", "tag1"},
                {TPID0X88A8.class, "88A8", "tag2"},
                {TPID0X9100.class, "9100", null},
                {TPID0X9200.class, "9200", null}
        });
    }

    @Parameterized.Parameter
    public Class<? extends TPIDTYPES> tpId;

    @Parameterized.Parameter(1)
    public String tpIdExpect;

    @Parameterized.Parameter(2)
    public String tpTagExpect;


    @Test
    public void writeTest() throws Exception {
        Config1 dataAfter = new Config1Builder().setTpid(tpId).build();

        Assert.assertEquals(tpIdExpect, TpIdInterfaceWriter.getTpIdForDevice(dataAfter));
        String tpIdTag;
        try {
            tpIdTag = TpIdInterfaceWriter.getTpIdTag(dataAfter);
        } catch (IllegalArgumentException e) {
            tpIdTag = null;
        }
        Assert.assertEquals(tpTagExpect, tpIdTag);
    }
}
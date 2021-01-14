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

package io.frinx.cli.unit.ios.snmp.handler;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.config.Mib;

public class ViewConfigReaderTest {

    private static final String OUTPUT = "snmp-server view SnmpReadAccess iso included\n"
            + "snmp-server view SnmpReadAccess internet excluded\n"
            + "snmp-server view SnmpReadAccess ifMIB excluded\n";

    @Test
    public void testViewMibs() {
        final List<Mib> mibs = ViewConfigReader.getMibs(OUTPUT);

        Assert.assertEquals(3, mibs.size());

        Assert.assertEquals("iso", mibs.get(0).getName());
        Assert.assertEquals(Mib.Inclusion.Included, mibs.get(0).getInclusion());

        Assert.assertEquals("internet", mibs.get(1).getName());
        Assert.assertEquals(Mib.Inclusion.Excluded, mibs.get(1).getInclusion());

        Assert.assertEquals("ifMIB", mibs.get(2).getName());
        Assert.assertEquals(Mib.Inclusion.Excluded, mibs.get(2).getInclusion());
    }

}
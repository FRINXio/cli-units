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

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.view.top.views.ViewKey;

public class ViewReaderTest {

    private static final String OUTPUT = "snmp-server view SnmpReadAccess ifMIB excluded\n"
            + "snmp-server view SnmpReadAccess internet excluded\n"
            + "snmp-server view SnmpWriteAccess iso included\n";

    @Test
    public void testViewKeys() {
        final List<ViewKey> keys = ViewReader.getViewKeys(OUTPUT);
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals(Lists.newArrayList("SnmpReadAccess", "SnmpWriteAccess"),
                keys.stream().map(ViewKey::getName).collect(Collectors.toList()));
    }

}
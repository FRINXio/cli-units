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

package io.frinx.cli.platform.handler;

import com.google.common.collect.Lists;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;

public class ComponentReaderTest {

    public static final String OUTPUT = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  1   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL0918ACD8\n" +
            "  2   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL093481KB\n" +
            "  3   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL09222BD3\n" +
            "  4   24  CEF720 24 port 1000mb SFP              WS-X6724-SFP       SAL09306BH9\n" +
            "  5    2  Supervisor Engine 720 (Active)         WS-SUP720-3B       SAL092961PK\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  1  0013.c424.a638 to 0013.c424.a667  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "  2  0015.2b68.6268 to 0015.2b68.6297  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "  3  0014.1c1e.f200 to 0014.1c1e.f22f  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "  4  0014.f2f2.9028 to 0014.f2f2.903f   2.3   12.2(14r)S5  12.2(33)SXJ1 Ok\n" +
            "  5  0014.a97d.9eac to 0014.a97d.9eaf   4.4   8.1(3)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Sub-Module                  Model              Serial       Hw     Status\n" +
            "---- --------------------------- ------------------ ----------- ------- -------\n" +
            "  2  Cisco Voice Daughter Card   WS-F6K-VPWR-GE     SAL09327A70  1.1    Ok\n" +
            "  3  IEEE Voice Daughter Card    WS-F6K-GE48-AF     SAL09222663  1.2    Ok\n" +
            "  4  Centralized Forwarding Card WS-F6700-CFC       SAL09465E6G  2.0    Ok\n" +
            "  5  Policy Feature Card 3       WS-F6K-PFC3B       SAL09306ANA  2.1    Ok\n" +
            "  5  MSFC3 Daughterboard         WS-SUP720          SAL09295MYH  2.3    Ok\n" +
            "\n" +
            "Mod  Online Diag Status\n" +
            "---- -------------------\n" +
            "  1  Pass\n" +
            "  2  Pass\n" +
            "  3  Pass\n" +
            "  4  Pass\n" +
            "  5  Pass";

    @Test
    public void testAllIds() throws Exception {
        Assert.assertEquals(
                Lists.newArrayList(1, 2, 3, 4, 5)
                        .stream()
                        .map(Object::toString)
                        .map(ComponentKey::new)
                        .collect(Collectors.toList()),

                        ComponentReader.getComponents(OUTPUT));
    }
}
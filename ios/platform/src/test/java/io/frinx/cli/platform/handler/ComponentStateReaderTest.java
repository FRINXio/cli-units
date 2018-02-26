/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.platform.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;

public class ComponentStateReaderTest {

    public static final String OUTPUT2 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  2   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL093481KB\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  2  0015.2b68.6268 to 0015.2b68.6297  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Sub-Module                  Model              Serial       Hw     Status \n" +
            "---- --------------------------- ------------------ ----------- ------- -------\n" +
            "  2  Cisco Voice Daughter Card   WS-F6K-VPWR-GE     SAL09327A70  1.1    Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  2  Pass\n";

    public static final String OUTPUT1 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  1   48  SFM-capable 48 port 10/100/1000mb RJ45 WS-X6548-GE-TX     SAL0918ACD8\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  1  0013.c424.a638 to 0013.c424.a667  10.1   7.2(1)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  1  Pass\n";

    public static final String OUTPUT5 = "Mod Ports Card Type                              Model              Serial No.\n" +
            "--- ----- -------------------------------------- ------------------ -----------\n" +
            "  5    2  Supervisor Engine 720 (Active)         WS-SUP720-3B       SAL092961PK\n" +
            "\n" +
            "Mod MAC addresses                       Hw    Fw           Sw           Status\n" +
            "--- ---------------------------------- ------ ------------ ------------ -------\n" +
            "  5  0014.a97d.9eac to 0014.a97d.9eaf   4.4   8.1(3)       12.2(33)SXJ1 Ok\n" +
            "\n" +
            "Mod  Sub-Module                  Model              Serial       Hw     Status \n" +
            "---- --------------------------- ------------------ ----------- ------- -------\n" +
            "  5  Policy Feature Card 3       WS-F6K-PFC3B       SAL09306ANA  2.1    Ok\n" +
            "  5  MSFC3 Daughterboard         WS-SUP720          SAL09295MYH  2.3    Ok\n" +
            "\n" +
            "Mod  Online Diag Status \n" +
            "---- -------------------\n" +
            "  5  Pass\n";


    @Test
    public void testParse() throws Exception {
        StateBuilder stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "2", OUTPUT2);
        assertEquals(new StateBuilder()
                        .setId("2")
                        .setDescription("SFM-capable 48 port 10/100/1000mb RJ45")
                        .setName("2")
                        .setPartNo("WS-X6548-GE-TX")
                        .setVersion("10.1")
                        .setSerialNo("SAL093481KB")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());

        stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "1", OUTPUT1);
        assertEquals(new StateBuilder()
                        .setId("1")
                        .setDescription("SFM-capable 48 port 10/100/1000mb RJ45")
                        .setName("1")
                        .setPartNo("WS-X6548-GE-TX")
                        .setVersion("10.1")
                        .setSerialNo("SAL0918ACD8")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());

        stateBuilder = new StateBuilder();
        ComponentStateReader.parseFields(stateBuilder, "5", OUTPUT5);
        assertEquals(new StateBuilder()
                        .setId("5")
                        .setDescription("Supervisor Engine 720 (Active)")
                        .setName("5")
                        .setPartNo("WS-SUP720-3B")
                        .setVersion("4.4")
                        .setSerialNo("SAL092961PK")
                        .setType(new PlatformComponentState.Type(LINECARD.class))
                        .build(),

                stateBuilder.build());
    }
}
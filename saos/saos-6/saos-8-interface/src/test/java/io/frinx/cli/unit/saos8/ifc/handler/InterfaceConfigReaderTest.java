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

package io.frinx.cli.unit.saos8.ifc.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;

public class InterfaceConfigReaderTest {

    private static final String SH_PORT_21 = "port set port 2/1 max-frame-size 9216\n"
        + "port set port 2/10 max-frame-size 9216 description \"to-meetkamer CPE21-Gi0/0/0\"\n"
        + "port set port 2/11 max-frame-size 9216 description TEMP_CS501-LM01W\n"
        + "port disable port 2/12\n"
        + "port disable port 2/13\n"
        + "port disable port 2/14\n"
        + "port disable port 2/15\n"
        + "port disable port 2/16\n"
        + "port disable port 2/17\n"
        + "port disable port 2/18\n"
        + "port disable port 2/19\n"
        + "port set port 2/10 speed gigabit auto-neg on\n"
        + "port set port 2/15 speed gigabit\n"
        + "sub-port create sub-port 1_2_10_ETREE-Dusko_1 parent-port 2/10 classifier-precedence 100 "
        + "ingress-l2-transform pop egress-l2-transform push-88a8.3500.map resolved-cos-policy "
        + "fixed-cos resolved-cos-profile fixed_16\n"
        + "sub-port create sub-port 1_2_18_OpticTest1_1 parent-port 2/18 classifier-precedence 100\n"
        + "sub-port create sub-port 1_2_17_OpticTest1_1 parent-port 2/17 classifier-precedence 100\n"
        + "sub-port create sub-port 1_2_16_OpticTest2_1 parent-port 2/16 classifier-precedence 100\n"
        + "sub-port create sub-port 1_2_15_OpticTest2_1 parent-port 2/15 classifier-precedence 100\n"
        + "sub-port create sub-port foo parent-port 2/16 classifier-precedence 1\n"
        + "aggregation add agg LM01W port 2/11\n"
        + "lldp set port 2/1-2/20 notification on\n";

    private static final Config EXPECTED_INTERFACE_21 = new ConfigBuilder()
            .setEnabled(true)
            .setName("2/1")
            .setMtu(9216)
            .build();

    private static final String SH_PORT_29 = "port set port 2/9 max-frame-size 9216 description TEMP_CS501-LM01WE\n"
            + "aggregation add agg LM01E port 2/9\n";

    private static final Config EXPECTED_INTERFACE_29 = new ConfigBuilder()
            .setEnabled(true)
            .setName("2/9")
            .setDescription("TEMP_CS501-LM01WE")
            .setMtu(9216)
            .build();

    private static final String SH_PORT_313 = "port disable port 3/13\n"
            + "aggregation add agg JMEP port 3/13\n"
            + "aggregation set port 3/13 agg-mode manual\n"
            + "snmp port-traps disable port 3/13 all\n"
            + "snmp port-traps disable port 3/13 link-up-down-trap enhanced\n"
            + "snmp port-traps disable port 3/13 link-up-down-trap standard\n";

    private static final Config EXPECTED_INTERFACE_313 = new ConfigBuilder()
            .setEnabled(false)
            .setName("3/13")
            .build();

    private static final String SH_AGG = "aggregation create agg LS01E\n";

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_21, parsed,"2/1");
        Assert.assertEquals(EXPECTED_INTERFACE_21, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_29, parsed, "2/9");
        Assert.assertEquals(EXPECTED_INTERFACE_29, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_313, parsed, "3/13");
        Assert.assertEquals(EXPECTED_INTERFACE_313, parsed.build());
    }

    @Test
    public void testParseInterfaceType() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed,"2/1");
        Assert.assertEquals(EthernetCsmacd.class, parsed.getType());

        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseType(SH_AGG, parsed, "LS01E");
        Assert.assertEquals(Ieee8023adLag.class, parsed.getType());
    }
}
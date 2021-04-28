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

package io.frinx.cli.unit.iosxe.bgp.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;

public class GlobalAfiSafiConfigWriterTest {

    GlobalAfiSafiConfigWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new GlobalAfiSafiConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest_default_NI() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "auto-summary\n"
                        + "end",
                writer.writeTemplate(65333L, null, null, createConfig(true, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null, createConfig(false, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null, createConfig(null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null, createConfig(null, true, true)));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "bgp router-id 0.0.0.0\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "bgp router-id 0.0.0.0\n"
                        + "redistribute connected\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(true, true, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "redistribute static\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, true)));
    }

    @Test
    public void updateTemplate_default_NI() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "no auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null),
                        createConfig(false, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(false, null, null),
                        createConfig(true, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "no auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null),
                        createConfig(false, true, true)));
    }

    @Test
    public void updateTemplate() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(true, null, null),
                        createConfig(false, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "redistribute connected\n"
                        + "redistribute static\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, null, null),
                        createConfig(null, true, true)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, true),
                        createConfig(null, true, true)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "no redistribute connected\n"
                        + "no redistribute static\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, true),
                        createConfig(null, false, false)));

    }

    private Config createConfig(Boolean autoSummary, Boolean redistCon, Boolean redistStat) {
        ConfigBuilder builder = new ConfigBuilder().setAfiSafiName(IPV4UNICAST.class);

        if (autoSummary != null || redistCon != null || redistStat != null) {
            GlobalAfiSafiConfigAugBuilder augBuilder = new GlobalAfiSafiConfigAugBuilder();

            if (autoSummary != null) {
                augBuilder.setAutoSummary(autoSummary);
            }
            if (redistCon != null) {
                augBuilder.setRedistributeConnected(redistCon);
            }
            if (redistStat != null) {
                augBuilder.setRedistributeStatic(redistStat);
            }

            return builder.addAugmentation(GlobalAfiSafiConfigAug.class, augBuilder.build()).build();
        }

        return builder.build();
    }
}


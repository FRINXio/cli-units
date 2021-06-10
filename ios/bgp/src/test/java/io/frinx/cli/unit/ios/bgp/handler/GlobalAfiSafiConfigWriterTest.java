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

package io.frinx.cli.unit.ios.bgp.handler;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeConnectedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeStaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;

public class GlobalAfiSafiConfigWriterTest {

    private GlobalAfiSafiConfigWriter writer;

    @Before
    public void setUp() {
        writer = new GlobalAfiSafiConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest_default_NI() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "auto-summary\n"
                        + "end",
                writer.writeTemplate(65333L, null, null,
                        createConfig(true, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null,
                        createConfig(false, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null,
                        createConfig(null, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "end",
                writer.writeTemplate(65333L, null, null,
                        createConfig(null, true, null, true, null, true, true)));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "bgp router-id 0.0.0.0\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(null, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "bgp router-id 0.0.0.0\n"
                        + "redistribute connected route-map FOO\n"
                        + "default-information originate\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(true, true, "FOO", null, null, true, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf vlan12\n"
                        + "redistribute static route-map BAR\n"
                        + "synchronization\n"
                        + "end",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, null, true, "BAR", null, true)));
    }

    @Test
    public void updateTemplate_default_NI() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "no auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null, null, null, null, null),
                        createConfig(false, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(false, null, null, null, null, null, null),
                        createConfig(true, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4\n"
                        + "no auto-summary\n"
                        + "end",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null, null, null, null, null),
                        createConfig(false, true, null, true, null, true, true)));
    }

    @Test
    public void updateTemplate() {
        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(true, null, null, null, null, null, null),
                        createConfig(false, null, null, null, null, null, null)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "no redistribute connected\n"
                        + "redistribute connected\n"
                        + "redistribute static route-map BAR\n"
                        + "default-information originate\n"
                        + "synchronization\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", null, null, null, null),
                        createConfig(null, true, null, true, "BAR", true, true)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", true, "BAR", true, true),
                        createConfig(null, true, "FOO", true, "BAR", true, true)));

        Assert.assertEquals("configure terminal\n"
                        + "router bgp 65333\n"
                        + "address-family ipv4 vrf VLAN1234\n"
                        + "no redistribute connected\n"
                        + "no redistribute static\n"
                        + "no default-information originate\n"
                        + "no synchronization\n"
                        + "end",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", true, "BAR", true, true),
                        createConfig(null, false, "FOO", false, "BAR", false, false)));

    }

    private Config createConfig(Boolean autoSummary,
                                Boolean redistCon,
                                String redistConRouteMap,
                                Boolean redistStat,
                                String redistStatRouteMap,
                                Boolean defaultInf,
                                Boolean sync) {
        ConfigBuilder builder = new ConfigBuilder().setAfiSafiName(IPV4UNICAST.class);

        if (autoSummary != null || redistCon != null || redistStat != null || defaultInf != null || sync != null) {
            GlobalAfiSafiConfigAugBuilder augBuilder = new GlobalAfiSafiConfigAugBuilder();
            augBuilder.setAutoSummary(autoSummary);
            if (redistCon != null) {
                augBuilder.setRedistributeConnected(new RedistributeConnectedBuilder()
                        .setEnabled(redistCon)
                        .setRouteMap(redistConRouteMap)
                        .build());
            }
            if (redistStat != null) {
                augBuilder.setRedistributeStatic(new RedistributeStaticBuilder()
                        .setEnabled(redistStat)
                        .setRouteMap(redistStatRouteMap)
                        .build());
            }
            augBuilder.setDefaultInformationOriginate(defaultInf);
            augBuilder.setSynchronization(sync);
            builder.addAugmentation(GlobalAfiSafiConfigAug.class, augBuilder.build());
        }

        return builder.build();
    }
}

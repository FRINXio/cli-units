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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.frinx.cli.io.Cli;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.GlobalAfiSafiConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeConnectedBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.global.afi.safi.config.extension.RedistributeStaticBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.yang.rev170403.DottedQuad;

class GlobalAfiSafiConfigWriterTest {

    private GlobalAfiSafiConfigWriter writer;

    @BeforeEach
    void setUp() {
        writer = new GlobalAfiSafiConfigWriter(Mockito.mock(Cli.class));
    }

    @Test
    void writeTemplateTest_default_NI() {
        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        auto-summary
                        end""",
                writer.writeTemplate(65333L, null, null,
                        createConfig(true, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        end""",
                writer.writeTemplate(65333L, null, null,
                        createConfig(false, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        end""",
                writer.writeTemplate(65333L, null, null,
                        createConfig(null, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        end""",
                writer.writeTemplate(65333L, null, null,
                        createConfig(null, true, null, true, null, true, true, "FOO")));
    }

    @Test
    void writeTemplateTest() {
        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf vlan12
                        end""",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf vlan12
                        bgp router-id 0.0.0.0
                        end""",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(null, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf vlan12
                        bgp router-id 0.0.0.0
                        redistribute connected route-map FOO
                        default-information originate
                        table-map FOO filter
                        end""",
                writer.writeTemplate(65333L, "vlan12", new DottedQuad("0.0.0.0"),
                        createConfig(true, true, "FOO", null, null, true, null, "FOO")));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf vlan12
                        redistribute static route-map BAR
                        synchronization
                        end""",
                writer.writeTemplate(65333L, "vlan12", null,
                        createConfig(null, null, null, true, "BAR", null, true, null)));
    }

    @Test
    void updateTemplate_default_NI() {
        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        no auto-summary
                        end""",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null, null, null, null, null, null),
                        createConfig(false, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        auto-summary
                        end""",
                writer.updateTemplate(65333L, null,
                        createConfig(false, null, null, null, null, null, null, null),
                        createConfig(true, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4
                        no auto-summary
                        end""",
                writer.updateTemplate(65333L, null,
                        createConfig(true, null, null, null, null, null, null, null),
                        createConfig(false, true, null, true, null, true, true, "FOO")));
    }

    @Test
    void updateTemplate() {
        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf VLAN1234
                        end""",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(true, null, null, null, null, null, null, null),
                        createConfig(false, null, null, null, null, null, null, null)));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf VLAN1234
                        no redistribute connected
                        redistribute connected
                        redistribute static route-map BAR
                        default-information originate
                        synchronization
                        table-map BAR filter
                        end""",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", null, null, null, null, null),
                        createConfig(null, true, null, true, "BAR", true, true, "BAR")));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf VLAN1234
                        end""",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", true, "BAR", true, true, "FOO"),
                        createConfig(null, true, "FOO", true, "BAR", true, true, "FOO")));

        assertEquals("""
                        configure terminal
                        router bgp 65333
                        address-family ipv4 vrf VLAN1234
                        no redistribute connected
                        no redistribute static
                        no default-information originate
                        no synchronization
                        no table-map
                        end""",
                writer.updateTemplate(65333L, "VLAN1234",
                        createConfig(null, true, "FOO", true, "BAR", true, true, "FOOBAR"),
                        createConfig(null, false, "FOO", false, "BAR", false, false, null)));

    }

    private Config createConfig(Boolean autoSummary,
                                Boolean redistCon,
                                String redistConRouteMap,
                                Boolean redistStat,
                                String redistStatRouteMap,
                                Boolean defaultInf,
                                Boolean sync,
                                String tableMap) {
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
            augBuilder.setTableMap(tableMap);
            builder.addAugmentation(GlobalAfiSafiConfigAug.class, augBuilder.build());
        }

        return builder.build();
    }
}

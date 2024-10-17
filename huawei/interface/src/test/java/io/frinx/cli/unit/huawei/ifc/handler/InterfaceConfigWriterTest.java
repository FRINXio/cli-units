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

package io.frinx.cli.unit.huawei.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.io.Command;
import io.frinx.openconfig.openconfig.interfaces.IIDs;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.HuaweiIfExtensionConfig.Trust;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.IfHuaweiAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.TrafficDirection.Direction;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficFilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.huawei.extension.rev210729.huawei._if.extension.config.TrafficPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

class InterfaceConfigWriterTest {

    private static final String PHYSICAL_INT_INPUT = """
            system-view
            interface 1
            description test - ethernet
            undo shutdown
            ip binding vpn-instance VLAN27
            set flow-stat interval 10
            traffic-filter Inbound ipv6 acl name WAN-IN
            traffic-policy TP-NNI-MAIN-OUT Outbound
            trust dscp
            lldp enable
            arp expire-time 60
            return
            """;

    private static final String PHYSICAL_INT_CLEAN_INPUT = """
            system-view
            interface 1
            undo description
            shutdown
            undo ip binding vpn-instance VLAN27
            undo set flow-stat interval
            undo traffic-filter Inbound
            undo traffic-policy Outbound
            undo trust
            undo lldp enable
            undo arp expire-time
            return
            """;

    private static final String OTHER_INT_WRITE_INPUT = """
            system-view
            interface 1
            shutdown
            undo set flow-stat interval
            undo trust
            lldp enable
            arp expire-time 60
            return
            """;

    private static final String OTHER_INT_DELETE_INPUT = """
            system-view
            undo interface 1
            return
            """;

    private static final Config PHYSICAL_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .build();

    private static final Config OTHER_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(Other.class)
            .setEnabled(false)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setLldpEnabled(true)
                    .setExpireTimeout(60L)
                    .build())
            .build();

    private static final Config PHYSICAL_INT_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(EthernetCsmacd.class)
            .setDescription("test - ethernet")
            .setEnabled(true)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setTrust(Trust.Dscp)
                    .setFlowStatInterval(10L)
                    .setLldpEnabled(true)
                    .setIpBindingVpnInstance("VLAN27")
                    .setExpireTimeout(60L)
                    .setTrafficFilter(new TrafficFilterBuilder()
                            .setDirection(Direction.Inbound)
                            .setAclName("WAN-IN")
                            .setIpv6(true)
                            .build())
                    .setTrafficPolicy(new TrafficPolicyBuilder()
                            .setDirection(Direction.Outbound)
                            .setTrafficPolicy("TP-NNI-MAIN-OUT")
                            .build())
                    .build())
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;
    private final InstanceIdentifier<Config> iid = IIDs.IN_IN_CONFIG;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceConfigWriter(cli);
    }

    @Test
    void writeOtherInterface() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, OTHER_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(OTHER_INT_WRITE_INPUT));
    }

    @Test
    void updatePhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG, PHYSICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(PHYSICAL_INT_INPUT));
    }

    @Test
    void updateCleanPhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CONFIG, PHYSICAL_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(PHYSICAL_INT_CLEAN_INPUT));
    }

    @Test
    void deleteOtherInterface() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, OTHER_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(OTHER_INT_DELETE_INPUT));
    }
}
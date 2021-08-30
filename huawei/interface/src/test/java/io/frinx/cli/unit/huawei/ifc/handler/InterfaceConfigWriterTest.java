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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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

public class InterfaceConfigWriterTest {

    private static final String PHYSICAL_INT_INPUT = "system-view\n"
            + "interface 1\n"
            + "description test - ethernet\n"
            + "no shutdown\n"
            + "ip binding vpn-instance VLAN27\n"
            + "set flow-stat interval 10\n"
            + "traffic-filter Inbound acl name WAN-IN\n"
            + "traffic-policy TP-NNI-MAIN-OUT Outbound\n"
            + "trust dscp\n"
            + "return\n";

    private static final String PHYSICAL_INT_CLEAN_INPUT = "system-view\n"
            + "interface 1\n"
            + "undo description\n"
            + "shutdown\n"
            + "undo ip binding vpn-instance VLAN27\n"
            + "undo set flow-stat interval\n"
            + "undo traffic-filter Inbound\n"
            + "undo traffic-policy Outbound\n"
            + "undo trust\n"
            + "return\n";

    private static final String OTHER_INT_WRITE_INPUT = "system-view\n"
            + "interface 1\n"
            + "shutdown\n"
            + "undo set flow-stat interval\n"
            + "undo trust\n"
            + "return\n";

    private static final String OTHER_INT_DELETE_INPUT = "system-view\n"
            + "undo interface 1\n"
            + "return\n";

    private static final Config PHYSICAL_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .build();

    private static final Config OTHER_INT_CLEAN_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(Other.class)
            .setEnabled(false)
            .build();

    private static final Config PHYSICAL_INT_CONFIG = new ConfigBuilder()
            .setName("1")
            .setType(EthernetCsmacd.class)
            .setDescription("test - ethernet")
            .setEnabled(true)
            .addAugmentation(IfHuaweiAug.class, new IfHuaweiAugBuilder()
                    .setTrustDscp(true)
                    .setFlowStatInterval(10L)
                    .setIpBindingVpnInstance("VLAN27")
                    .setTrafficFilter(new TrafficFilterBuilder()
                            .setDirection(Direction.Inbound)
                            .setAclName("WAN-IN")
                            .build())
                    .setTrafficPolicy(new TrafficPolicyBuilder()
                            .setDirection(Direction.Outbound)
                            .setTrafficName("TP-NNI-MAIN-OUT")
                            .build())
                    .build())
            .build();

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private InterfaceConfigWriter writer;
    private final InstanceIdentifier<Config> iid = IIDs.IN_IN_CONFIG;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));
        writer = new InterfaceConfigWriter(cli);
    }

    @Test
    public void writeOtherInterface() throws WriteFailedException {
        writer.writeCurrentAttributes(iid, OTHER_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(OTHER_INT_WRITE_INPUT));
    }

    @Test
    public void updatePhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CLEAN_CONFIG, PHYSICAL_INT_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(PHYSICAL_INT_INPUT));
    }

    @Test
    public void updateCleanPhysical() throws WriteFailedException {
        writer.updateCurrentAttributes(iid, PHYSICAL_INT_CONFIG, PHYSICAL_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(PHYSICAL_INT_CLEAN_INPUT));
    }

    @Test
    public void deleteOtherInterface() throws WriteFailedException {
        writer.deleteCurrentAttributes(iid, OTHER_INT_CLEAN_CONFIG, context);
        Mockito.verify(cli).executeAndRead(Command.writeCommand(OTHER_INT_DELETE_INPUT));
    }
}

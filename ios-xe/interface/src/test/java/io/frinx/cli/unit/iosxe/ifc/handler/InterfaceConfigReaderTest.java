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

package io.frinx.cli.unit.iosxe.ifc.handler;

import io.frinx.cli.io.Cli;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.IfCiscoExtAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.config.EncapsulationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.ServiceInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.service.instance.top.service.instances.ServiceInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControl;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cisco.rev171024.storm.control.StormControlKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final List<StormControl> STORM_CONTROL_LIST = Arrays.asList(
            new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Broadcast))
                    .setAddress(StormControl.Address.Broadcast)
                    .setLevel(new BigDecimal("10.00"))
                    .build(),
            new StormControlBuilder()
                    .setKey(new StormControlKey(StormControl.Address.Unicast))
                    .setAddress(StormControl.Address.Unicast)
                    .setLevel(new BigDecimal("10.00"))
                    .build()
    );

    private static final ServiceInstances SERVICE_INSTANCES = new ServiceInstancesBuilder()
            .setServiceInstance(Arrays.asList(
                    new ServiceInstanceBuilder()
                            .setId(100L)
                            .setKey(new ServiceInstanceKey(100L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(100L)
                                    .setTrunk(false)
                                    .setEvc("EVC")
                                    .setEncapsulation(new EncapsulationBuilder()
                                            .setUntagged(true)
                                            .setDot1q(Arrays.asList(1, 2, 3, 5, 6, 7, 8, 9, 10))
                                            .build())
                                    .build())
                            .build(),
                    new ServiceInstanceBuilder()
                            .setId(200L)
                            .setKey(new ServiceInstanceKey(200L))
                            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang
                                    .interfaces.cisco.rev171024.service.instance.top.service.instances
                                    .service.instance.ConfigBuilder()
                                    .setId(200L)
                                    .setTrunk(true)
                                    .build())
                            .build()
            ))
            .build();

    public static final IfCiscoExtAug IF_CISCO_EXT_AUG = new IfCiscoExtAugBuilder()
            .setStormControl(STORM_CONTROL_LIST)
            .setLldpTransmit(false)
            .setLldpReceive(false)
            .setServiceInstances(SERVICE_INSTANCES)
            .build();

    public static final IfSaosAug IF_SAOS_AUG = new IfSaosAugBuilder()
            .setPhysicalType(SaosIfExtensionConfig.PhysicalType.Rj45)
            .build();

    private static final Config EXPECTED_INTERFACE = new ConfigBuilder()
            .setEnabled(false)
            .setName("GigabitEthernet0/0/0")
            .setType(EthernetCsmacd.class)
            .setDescription("test - description")
            .setMtu(1530)
            .addAugmentation(IfCiscoExtAug.class, IF_CISCO_EXT_AUG)
            .addAugmentation(IfSaosAug.class, IF_SAOS_AUG)
            .build();

    private static final String SH_INTERFACE_RUN = "interface GigabitEthernet0/0/0\n"
            + " mtu 1530\n"
            + " no ip address\n"
            + " shutdown\n"
            + " negotiation auto\n"
            + " description test - description\n"
            + " no lldp transmit\n"
            + " no lldp receive\n"
            + " media-type rj45\n"
            + " storm-control broadcast level 10.00 \n"
            + " storm-control unicast level 10.00 \n"
            + " !\n"
            + " service instance 100 ethernet EVC\n"
            + "  encapsulation untagged , dot1q 1-3,5-10\n"
            + " !\n"
            + " service instance trunk 200 ethernet\n"
            + " !\n"
            + "end\n";

    @Test
    public void testParseInterface() {
        final ConfigBuilder configBuilder = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_INTERFACE_RUN, configBuilder, "GigabitEthernet0/0/0");
        Assert.assertEquals(EXPECTED_INTERFACE, configBuilder.build());
    }

}
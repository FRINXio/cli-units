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

package io.frinx.cli.unit.iosxr.snmp.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.ConfigBuilder;

public class SnmpInterfacesReaderTest {

    private static final String SH_RUN_SNMP_SERVER = "Thu Feb 15 11:40:49.064 UTC\n"
            + "snmp-server interface Bundle-Ether1\n"
            + "snmp-server interface Bundle-Ether7000\n"
            + "snmp-server interface tunnel-te55\n"
            + " notification linkupdown disable\n"
            + "snmp-server interface tunnel-te56\n"
            + " notification linkupdown disable\n"
            + "snmp-server interface GigabitEthernet0/0/0/1.100\n"
            + "snmp-server interface GigabitEthernet0/0/0/4\n"
            + " notification linkupdown disable";

    private static final HashSet<String> DISABLED = Sets.newHashSet("tunnel-te55", "tunnel-te56",
            "GigabitEthernet0/0/0/4");

    private static final List<Interface> INTERFACE_LIST =
            Lists.newArrayList("Bundle-Ether1", "Bundle-Ether7000", "tunnel-te55", "tunnel-te56",
                    "GigabitEthernet0/0/0/1.100", "GigabitEthernet0/0/0/4")
                    .stream()
                    .map(InterfaceId::new)
                    .map(ifcId -> new ConfigBuilder()
                            .setInterfaceId(ifcId)
                            .setEnabledTrapForEvent(DISABLED.contains(ifcId.getValue())
                                    ? SnmpInterfacesReader.LINK_UP_DOWN_EVENT_LIST_DISABLED
                                    : SnmpInterfacesReader.LINK_UP_DOWN_EVENT_LIST)
                            .build())
                    .map(config -> new InterfaceBuilder()
                            .setInterfaceId(config.getInterfaceId())
                            .setConfig(config)
                            .build())
                    .collect(Collectors.toList());

    private static final Interfaces EXPECTED_IFCS = new InterfacesBuilder()
            .setInterface(INTERFACE_LIST)
            .build();

    @Test
    public void testParseInterfaces() {
        InterfacesBuilder interfacesBuilder = new InterfacesBuilder();
        SnmpInterfacesReader.parseInterfaces(SH_RUN_SNMP_SERVER, interfacesBuilder);

        Assert.assertEquals(EXPECTED_IFCS, interfacesBuilder.build());
    }

}
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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEvent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp._interface.config.EnabledTrapForEventBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SnmpInterfacesReader implements CliConfigReader<Interfaces, InterfacesBuilder> {

    private static final String SH_RUN_SNM_IFCS =
            "show running-config snmp-server | utility egrep \"^snmp-server interface|^ notification linkupdown "
                    + "disable\"";

    // TODO try to reuse pattern from interface translate unit's interface reader
    private static final Pattern INTERFACE_ID = Pattern.compile(".*interface (?<id>\\S+)\\s*.*?"
            + "(?<linkupDisable>notification linkupdown disable)?");

    private static final EnabledTrapForEvent LINK_UP_DOWN_EVENT = new EnabledTrapForEventBuilder()
            .setEventName(LINKUPDOWN.class)
            .setEnabled(true)
            .build();

    private static final EnabledTrapForEvent LINK_UP_DOWN_EVENT_DISABLED = new EnabledTrapForEventBuilder()
            .setEventName(LINKUPDOWN.class)
            .setEnabled(false)
            .build();

    @VisibleForTesting
    static final List<EnabledTrapForEvent> LINK_UP_DOWN_EVENT_LIST =
            Collections.singletonList(LINK_UP_DOWN_EVENT);

    @VisibleForTesting
    static final List<EnabledTrapForEvent> LINK_UP_DOWN_EVENT_LIST_DISABLED
            = Collections.singletonList(LINK_UP_DOWN_EVENT_DISABLED);

    private Cli cli;

    public SnmpInterfacesReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interfaces> instanceIdentifier,
                                      @Nonnull InterfacesBuilder interfacesBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseInterfaces(blockingRead(SH_RUN_SNM_IFCS,
                cli, instanceIdentifier, readContext), interfacesBuilder);
    }

    @VisibleForTesting
    static void parseInterfaces(String snmpInterfaceOutput, InterfacesBuilder interfacesBuilder) {
        String realignedOutput = realignSnmpDisabledInterfacesOutput(snmpInterfaceOutput);

        List<Map.Entry<InterfaceKey, Boolean>> ifcKeyes = ParsingUtils.NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .map(INTERFACE_ID::matcher)
                .filter(Matcher::matches)
                .map(matcher -> new AbstractMap.SimpleEntry<>(matcher.group("id"),
                        matcher.group("linkupDisable") == null))
                .map(e -> new AbstractMap.SimpleEntry<>(new InterfaceId(e.getKey()), e.getValue()))
                .map(e -> new AbstractMap.SimpleEntry<>(new InterfaceKey(e.getKey()), e.getValue()))
                .collect(Collectors.toList());

        List<Interface> interfaceList = ifcKeyes.stream()
                .map(SnmpInterfacesReader::getIfcConfig)
                .map(SnmpInterfacesReader::getInterface)
                .collect(Collectors.toList());

        interfacesBuilder.setInterface(interfaceList);
    }

    private static Config getIfcConfig(Map.Entry<InterfaceKey, Boolean> ifcEntry) {
        return new ConfigBuilder()
                .setInterfaceId(ifcEntry.getKey()
                        .getInterfaceId())
                .setEnabledTrapForEvent(ifcEntry.getValue() ? LINK_UP_DOWN_EVENT_LIST :
                        LINK_UP_DOWN_EVENT_LIST_DISABLED)
                .build();
    }

    private static Interface getInterface(Config config) {
        return new InterfaceBuilder()
                .setConfig(config)
                .setInterfaceId(config.getInterfaceId())
                .build();
    }

    private static String realignSnmpDisabledInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), "");
        return withoutNewlines.replace("snmp-server", "\n");
    }
}

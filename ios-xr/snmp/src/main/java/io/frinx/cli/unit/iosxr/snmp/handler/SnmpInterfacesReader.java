/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.iosxr.snmp.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Collections;
import java.util.List;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.snmp.rev171024.snmp.top.SnmpBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SnmpInterfacesReader implements CliConfigReader<Interfaces, InterfacesBuilder> {

    private static final String SH_RUN_SNM_IFCS =
            "show running-config snmp-server | utility egrep \"^snmp-server interface|^ notification linkupdown disable\"";

    // TODO try to reuse pattern from interface translate unit's interface reader
    private static final Pattern INTERFACE_ID = Pattern.compile(".*interface (?<id>\\S+).*");

    private static final EnabledTrapForEvent LINK_UP_DOWN_EVENT = new EnabledTrapForEventBuilder()
            .setEventName(LINKUPDOWN.class)
            .build();

    static final List<EnabledTrapForEvent> LINK_UP_DOWN_EVENT_LIST =
            Collections.singletonList(LINK_UP_DOWN_EVENT);

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

        List<InterfaceKey> ifcKeyes = NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(ifcLine -> !ifcLine.contains("notification linkupdown disable"))
                .map(INTERFACE_ID::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .map(InterfaceId::new)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());

        List<Interface> interfaceList = ifcKeyes.stream().map(InterfaceKey::getInterfaceId)
                .map(SnmpInterfacesReader::getIfcConfig)
                .map(SnmpInterfacesReader::getInterface)
                .collect(Collectors.toList());

        interfacesBuilder.setInterface(interfaceList);
    }

    private static Config getIfcConfig(InterfaceId ifcId) {
        return new ConfigBuilder()
                .setInterfaceId(ifcId)
                .setEnabledTrapForEvent(LINK_UP_DOWN_EVENT_LIST)
                .build();
    }

    private static Interface getInterface(Config config) {
        return new InterfaceBuilder()
                .setConfig(config)
                .setInterfaceId(config.getInterfaceId())
                .build();
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Interfaces interfaces) {
        ((SnmpBuilder) builder).setInterfaces(interfaces);
    }

    private static String realignSnmpDisabledInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("snmp-server", "\n");
    }
}

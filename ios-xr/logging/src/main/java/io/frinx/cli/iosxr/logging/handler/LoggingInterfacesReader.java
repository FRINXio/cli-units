/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.logging.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.event.types.rev171024.LINKUPDOWN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging._interface.config.EnabledLoggingForEvent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging._interface.config.EnabledLoggingForEventBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.Interfaces;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.InterfacesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.interfaces.structural.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.logging.rev171024.logging.top.LoggingBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoggingInterfacesReader implements CliConfigReader<Interfaces, InterfacesBuilder> {

    private final Cli cli;

    private static final String SH_RUN_INTERFACE_LOGGING =
            "sh run | utility egrep \"^interface|^ logging events link-status\"";

    static final EnabledLoggingForEvent LINK_UP_DOWN_EVENT =
            new EnabledLoggingForEventBuilder()
                    .setEventName(LINKUPDOWN.class)
                    .build();

    static final List<EnabledLoggingForEvent> LINK_UP_DOWN_EVENT_LIST =
            Collections.singletonList(LINK_UP_DOWN_EVENT);

    public LoggingInterfacesReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public InterfacesBuilder getBuilder(@Nonnull InstanceIdentifier<Interfaces> id) {
        return new InterfacesBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interfaces> id, @Nonnull InterfacesBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String output = blockingRead(SH_RUN_INTERFACE_LOGGING, cli, id, ctx);
        String realignedOutput = realignLoggingEnabledInterfacesOutput(output);

        List<InterfaceKey> ifcKeyes = NEWLINE.splitAsStream(realignedOutput)
                .map(String::trim)
                .filter(ifcLine -> ifcLine.contains("logging events link-status"))
                .map(ifcLine -> ifcLine.split(" ")[0])
                .map(String::trim)
                .map(InterfaceId::new)
                .map(InterfaceKey::new)
                .collect(Collectors.toList());

        List<Interface> interfaceList = ifcKeyes.stream().map(InterfaceKey::getInterfaceId)
                .map(LoggingInterfacesReader::getIfcConfig)
                .map(LoggingInterfacesReader::getInterface)
                .collect(Collectors.toList());

        builder.setInterface(interfaceList);
    }

    private static Config getIfcConfig(InterfaceId ifcId) {
        return new ConfigBuilder()
                .setInterfaceId(ifcId)
                .setEnabledLoggingForEvent(LINK_UP_DOWN_EVENT_LIST)
                .build();
    }

    private static Interface getInterface(Config config) {
        return new InterfaceBuilder()
                .setConfig(config)
                .setInterfaceId(config.getInterfaceId())
                .build();
    }

    private static String realignLoggingEnabledInterfacesOutput(String output) {
        String withoutNewlines = output.replaceAll(NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }


    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Interfaces readValue) {
        ((LoggingBuilder) parentBuilder).setInterfaces(readValue);
    }
}

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

package io.frinx.cli.unit.iosxr.logging.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoggingInterfacesReader implements CliConfigReader<Interfaces, InterfacesBuilder> {

    private final Cli cli;

    private static final String SH_RUN_INTERFACE_LOGGING =
            "show running-config | utility egrep \"^interface|^ logging events link-status\"";

    static final EnabledLoggingForEvent LINK_UP_DOWN_EVENT =
            new EnabledLoggingForEventBuilder()
                    .setEventName(LINKUPDOWN.class)
                    .build();

    static final List<EnabledLoggingForEvent> LINK_UP_DOWN_EVENT_LIST =
            Collections.singletonList(LINK_UP_DOWN_EVENT);

    public LoggingInterfacesReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interfaces> id, @NotNull InterfacesBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        String output = blockingRead(SH_RUN_INTERFACE_LOGGING, cli, id, ctx);
        String realignedOutput = realignLoggingEnabledInterfacesOutput(output);

        List<InterfaceKey> ifcKeyes = ParsingUtils.NEWLINE.splitAsStream(realignedOutput)
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
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), "");
        return withoutNewlines.replace("interface ", "\n");
    }
}
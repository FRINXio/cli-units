/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class ComponentReader implements CliOperListReader<Component, ComponentKey, ComponentBuilder> {

    private Cli cli;

    public ComponentReader(Cli cli) {
        this.cli = cli;
    }

    public static final String TRANSCEIVER_PREFIX = "transceiver_";
    public static final String CHASSIS_PREFIX = "chassis_";
    public static final String VERSION_PREFIX = "version_";

    public static final String SH_PORT_TR = "show port transceiver";
    public static final String SH_CH_VERSION = "show version chassis detail";
    public static final String SH_VERSION = "show version detail";

    private static final Pattern PARSE_TRANSCEIVER = Pattern.compile("slot/port:\\s+(?<transceiver>\\d+/\\d+) *");
    private static final Pattern PARSE_CHASSIS = Pattern.compile("Module:\\s+(?<chassis>[a-zA-Z0-9 ]+) *");
    private static final Pattern PARSE_VERSION = Pattern.compile("Slot:\\s+(?<version>\\d+) *");

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Component> instanceIdentifier,
                                      ComponentBuilder componentBuilder,
                                      ReadContext readContext) {
        componentBuilder.setName(instanceIdentifier.firstKeyOf(Component.class).getName());
    }

    @NotNull
    @Override
    public List<ComponentKey> getAllIds(@NotNull InstanceIdentifier<Component> id,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        var componentKeys = parseAllTransceiver(blockingRead(SH_PORT_TR, cli, id, readContext));
        componentKeys.addAll(parseAllChassis(blockingRead(SH_CH_VERSION, cli, id, readContext)));
        componentKeys.addAll(parseAllVersion(blockingRead(SH_VERSION, cli, id, readContext)));
        componentKeys.add(OsComponent.OS_KEY);
        return componentKeys;
    }

    static List<ComponentKey> parseAllTransceiver(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_TRANSCEIVER::matcher,
            matcher -> matcher.group("transceiver"),
            v -> new ComponentKey(TRANSCEIVER_PREFIX + v));
    }

    static List<ComponentKey> parseAllChassis(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_CHASSIS::matcher,
            matcher -> matcher.group("chassis"),
            v -> new ComponentKey(CHASSIS_PREFIX + v));
    }

    static List<ComponentKey> parseAllVersion(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_VERSION::matcher,
            matcher -> matcher.group("version"),
            v -> new ComponentKey(VERSION_PREFIX + v));
    }
}
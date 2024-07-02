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

package io.frinx.cli.unit.saos8.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Saos8ComponentReader implements CliOperListReader<Component, ComponentKey, ComponentBuilder> {

    private Cli cli;

    public Saos8ComponentReader(Cli cli) {
        this.cli = cli;
    }

    public static final String DEVICE_ID = "DEV";
    public static final String MODULE_PREFIX = "module_";
    public static final String DEVICE_PREFIX = "dev_";
    public static final String IOM_PREFIX = "iom_";
    private static final String SH_DEV_ID = "chassis device-id show";
    private static final String SH_PORT_XCVR = "port xcvr show";
    private static final Pattern PARSE_MODULE = Pattern.compile(".*MODULE DEVICE ID SLOT (?<module>[^+]+)  *---*\\+");
    private static final Pattern PARSE_DEVICE_IDS = Pattern.compile(".*-- DEVICE ID SLOT (?<dev>[^+]+)  *---*\\+");
    private static final Pattern PARSE_IOM = Pattern.compile(".* (?<iom>IOM) DEVICE ID .*");
    static final Pattern PARSE_PORT = Pattern.compile("\\| (?<port>[0-9]+/[0-9]+).*\\|.*\\|.*\\|.*\\|.*");
    public static final String PORT_PREFIX_CONST = "port_";

    @Override
    public void readCurrentAttributes(InstanceIdentifier<Component> instanceIdentifier,
                                      ComponentBuilder componentBuilder,
                                      ReadContext readContext) {
        componentBuilder.setName(instanceIdentifier.firstKeyOf(Component.class).getName());
    }

    @NotNull
    @Override
    public List<ComponentKey> getAllIds(@NotNull InstanceIdentifier<Component> id,
                                        @NotNull ReadContext context) throws ReadFailedException {
        var componentKeys = parseAllModules(blockingRead(SH_DEV_ID, cli, id, context));
        componentKeys.addAll(parseAllDeviceIds(blockingRead(SH_DEV_ID, cli, id, context)));
        componentKeys.addAll(Collections.singletonList(new ComponentKey(DEVICE_ID)));
        componentKeys.addAll(parseIom(blockingRead(SH_DEV_ID, cli, id, context)));
        componentKeys.addAll(parseAllPortIds(blockingRead(SH_PORT_XCVR, cli, id, context)));
        return componentKeys;
    }

    static List<ComponentKey> parseAllModules(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_MODULE::matcher,
            matcher -> matcher.group("module"),
            v -> new ComponentKey(MODULE_PREFIX + v.trim()));
    }

    static List<ComponentKey> parseAllDeviceIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_DEVICE_IDS::matcher,
            matcher -> matcher.group("dev"),
            v -> new ComponentKey(DEVICE_PREFIX + v.trim()));
    }

    static List<ComponentKey> parseIom(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_IOM::matcher,
            matcher -> matcher.group("iom"),
            v -> new ComponentKey(IOM_PREFIX + v.trim()));
    }

    static List<ComponentKey> parseAllPortIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_PORT::matcher,
            matcher -> matcher.group("port"),
            v -> new ComponentKey(PORT_PREFIX_CONST + v));
    }
}
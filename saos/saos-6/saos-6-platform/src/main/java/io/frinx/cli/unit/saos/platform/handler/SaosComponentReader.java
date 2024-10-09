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

package io.frinx.cli.unit.saos.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SaosComponentReader implements CliOperListReader<Component, ComponentKey, ComponentBuilder> {

    private Cli cli;

    public SaosComponentReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_POWER = "chassis show power";
    private static final String SH_PORT_XCVR = "port xcvr show";
    public static final String POWER_SUPPLY_PREFIX = "power_supply_";
    public static final String PORT_PREFIX_CONST = "port_";
    static final Pattern PARSE_POWER = Pattern.compile("\\| (?<power>\\S+) +\\| \\S+ +\\| \\S+ +\\| (Online).*");
    static final Pattern PARSE_PORT = Pattern.compile("\\|(?<port>[0-9]+) +\\|(Ena) *.*");

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
        var componentKeys = parseAllPowerIds(blockingRead(SH_POWER, cli, id, context));
        componentKeys.addAll(parseAllPortIds(blockingRead(SH_PORT_XCVR, cli, id, context)));
        componentKeys.addAll(Collections.singletonList(OsComponent.OS_KEY));
        return componentKeys;
    }

    static List<ComponentKey> parseAllPowerIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_POWER::matcher,
            matcher -> matcher.group("power"),
            v -> new ComponentKey(POWER_SUPPLY_PREFIX + v));
    }

    static List<ComponentKey> parseAllPortIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            PARSE_PORT::matcher,
            matcher -> matcher.group("port"),
            v -> new ComponentKey(PORT_PREFIX_CONST + v));
    }
}
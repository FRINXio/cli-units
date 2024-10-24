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

package io.frinx.cli.unit.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.cisco.rev220620.CiscoPlatformAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_MODULE_INVENTORY = "show inventory";
    private static final String SH_MODULE_VERSION = "show version";
    static final Pattern VERSION = Pattern.compile("Cisco IOS Software.*Version (?<version>[[^\\s^,]]+).*");
    static final String IDS = "NAME: \"%s\".*DESCR: \"(?<description>[^\"]+)\".*PID: (?<pid>[^,]+),.*"
            + "VID: (?<vid>[^,]+),.*SN: (?<sn>[^,]+).*";

    private final Cli cli;

    public ComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        var name = instanceIdentifier.firstKeyOf(Component.class).getName();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOSVersions(stateBuilder, blockingRead(f(SH_MODULE_VERSION), cli, instanceIdentifier, readContext));
        } else {
            parseFields(stateBuilder, name, blockingRead(f(SH_MODULE_INVENTORY, name), cli, instanceIdentifier,
                    readContext));
        }
    }

    static void parseFields(@NotNull StateBuilder stateBuilder, String name, String output) {
        stateBuilder.setName(name);
        stateBuilder.setId(name);

        var ids = Pattern.compile(String.format(IDS, name));

        output = processOutput(output);

        var builder = new CiscoPlatformAugBuilder();

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("description"),
            stateBuilder::setDescription);

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("pid"),
            v -> builder.setPid(v.trim()));

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("vid"),
            v -> builder.setVid(v.trim()));

        ParsingUtils.parseField(output, 0,
            ids::matcher,
            m -> m.group("sn"),
            v -> builder.setSn(v.trim()));

        stateBuilder.addAugmentation(CiscoPlatformAug.class, builder.build());

        // TODO We are reading just line cards now, so it should be fine
        // to always set LINECARD type for now. But in the future we should
        // take into account also other types
        stateBuilder.setType(new PlatformComponentState.Type(LINECARD.class));
    }

    static void parseOSVersions(@NotNull StateBuilder stateBuilder, String output) {

        stateBuilder.setName(OsComponent.OS_NAME);
        ParsingUtils.parseField(output, 0,
            VERSION::matcher,
            m -> m.group("version"),
            stateBuilder::setVersion);

        ParsingUtils.parseField(output, 0,
            ComponentReader.LINE::matcher,
            m -> m.group("name"),
            stateBuilder::setName);
    }

    private static String processOutput(String output) {
        return output.replaceAll("PID", ",PID")
                .replaceAll("\\\\n", " ")
                .replaceAll("\\n", " ")
                .replaceAll("\\r", "")
                .replaceAll("NAME", "\nNAME")
                .replaceFirst("\\n", "");
    }
}
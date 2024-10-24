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

package io.frinx.cli.unit.iosxr.platform.handler;

import com.google.common.collect.Sets;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.CHASSIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.CPU;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.FAN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.OPENCONFIGHARDWARECOMPONENT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.POWERSUPPLY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.TRANSCEIVER;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class XrOsComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_MODULE_VERSION = "show version";

    private final Cli cli;

    public XrOsComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Component.class).getName();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOSVersions(stateBuilder, blockingRead(f(SH_MODULE_VERSION), cli, instanceIdentifier, readContext));
        } else {
            parseFields(stateBuilder, name, blockingRead(f(XrOsComponentReader.SH_MODULE, name),
                cli, instanceIdentifier,
                    readContext));
        }
    }

    private static final Map<Set<String>, Class<? extends OPENCONFIGHARDWARECOMPONENT>> HW_TYPES = new HashMap<>();

    static {
        HW_TYPES.put(Sets.newHashSet("linecard"), LINECARD.class);
        HW_TYPES.put(Sets.newHashSet("sfp", "transceiver"), TRANSCEIVER.class);
        HW_TYPES.put(Sets.newHashSet("fan"), FAN.class);
        HW_TYPES.put(Sets.newHashSet("chassis"), CHASSIS.class);
        HW_TYPES.put(Sets.newHashSet("power"), POWERSUPPLY.class);
        HW_TYPES.put(Sets.newHashSet("processor", "cpu"), CPU.class);
    }

    static void parseFields(@NotNull StateBuilder stateBuilder, String name, String output) {
        output = ParsingUtils.NEWLINE.splitAsStream(XrOsComponentReader.preprocessOutput(output))
            .filter(line -> line.contains(name))
            .findFirst()
            .orElse("");

        stateBuilder.setName(name);
        stateBuilder.setId(name);

        ParsingUtils.parseField(output, 0,
            XrOsComponentReader.LINE::matcher,
            m -> m.group("descr"),
            desc -> stateBuilder.setDescription(desc.trim()));

        ParsingUtils.parseField(output, 0,
            XrOsComponentReader.LINE::matcher,
            m -> m.group("sn"),
            desc -> stateBuilder.setSerialNo(desc.trim()));

        ParsingUtils.parseField(output, 0,
            XrOsComponentReader.LINE::matcher,
            m -> m.group("pid"),
            desc -> stateBuilder.setPartNo(desc.trim()));

        ParsingUtils.parseField(output, 0,
            XrOsComponentReader.LINE::matcher,
            m -> m.group("vid"),
            desc -> stateBuilder.setVersion(desc.trim()));

        for (var entry : HW_TYPES.entrySet()) {
            var typeNames = entry.getKey();
            var candidateType = entry.getValue();

            for (String typeName : typeNames) {
                if (stateBuilder.getDescription().toLowerCase(Locale.ROOT).contains(typeName)) {
                    stateBuilder.setType(new PlatformComponentState.Type(candidateType));
                    return;
                }
            }
        }
    }

    private static final Pattern ID_COMP = Pattern.compile("Cisco\\s(?<id>.+)\\sSoftware,.*");
    private static final Pattern VERSION = Pattern.compile(".*Version (?<version>.+)");

    static void parseOSVersions(@NotNull StateBuilder stateBuilder, String output) {

        stateBuilder.setName(OsComponent.OS_NAME);

        ParsingUtils.parseField(output, 0,
            ID_COMP::matcher,
            m -> m.group("id"),
            stateBuilder::setId);

        ParsingUtils.parseField(output, 0,
            VERSION::matcher,
            m -> m.group("version"),
            stateBuilder::setSoftwareVersion);

    }
}
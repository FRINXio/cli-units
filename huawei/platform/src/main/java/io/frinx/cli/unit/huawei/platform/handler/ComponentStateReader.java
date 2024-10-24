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
package io.frinx.cli.unit.huawei.platform.handler;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.OsComponent;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.CHASSIS;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.CPU;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.FAN;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.OPENCONFIGHARDWARECOMPONENT;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.POWERSUPPLY;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.TRANSCEIVER;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ComponentStateReader implements CliOperReader<State, StateBuilder> {

    private final Cli cli;

    public ComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_VERSION = "display version";

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<State> instanceIdentifier,
                                      @NotNull StateBuilder stateBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Component.class).getName();
        if (name.equals(OsComponent.OS_KEY.getName())) {
            parseOSVersions(stateBuilder, blockingRead(f(SH_VERSION), cli, instanceIdentifier, readContext));
        } else {
            parseFields(stateBuilder, name, blockingRead(f(ComponentReader.SH_MODULE, name), cli, instanceIdentifier,
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
        Matcher matcher = ComponentReader.LINE.matcher(output);

        while (matcher.find()) {
            String sn = matcher.group("sn").trim();
            if (sn.equals(name)) {
                break;
            }
        }

        stateBuilder.setName(name);
        stateBuilder.setId(name);

        stateBuilder.setDescription(matcher.group("descr").trim());
        stateBuilder.setSerialNo(matcher.group("sn").trim());
        stateBuilder.setPartNo(matcher.group("item").trim());
        stateBuilder.setVersion(matcher.group("issue").trim());

        for (var entry : HW_TYPES.entrySet()) {
            Set<String> typeNames = entry.getKey();
            Class<? extends OPENCONFIGHARDWARECOMPONENT> candidateType = entry.getValue();

            for (String typeName : typeNames) {
                if (stateBuilder.getDescription().toLowerCase(Locale.ROOT).contains(typeName)) {
                    stateBuilder.setType(new PlatformComponentState.Type(candidateType));
                    return;
                }
            }
        }
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> builder, @NotNull State state) {
        ((ComponentBuilder) builder).setState(state);
    }

    private static final Pattern ID_COMP = Pattern.compile("Huawei\\s(?<id>.+)\\sSoftware");
    private static final Pattern LINE_SW = Pattern.compile(".+, Version (?<version>.+)");

    static void parseOSVersions(@NotNull StateBuilder stateBuilder, String output) {

        stateBuilder.setName(OsComponent.OS_NAME);

        ParsingUtils.parseField(output, 0,
                ID_COMP::matcher,
            m -> m.group("id"),
            stateBuilder::setId);

        ParsingUtils.parseField(output, 0,
                LINE_SW::matcher,
            m -> m.group("version"),
            stateBuilder::setSoftwareVersion);
    }
}
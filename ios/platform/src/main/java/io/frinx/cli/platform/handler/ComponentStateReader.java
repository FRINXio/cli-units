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
package io.frinx.cli.platform.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliOperReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.PlatformComponentState;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.Component;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.ComponentBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.rev161222.platform.component.top.components.component.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.platform.types.rev170816.LINECARD;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

import static io.frinx.cli.platform.handler.ComponentReader.LINE;
import static io.frinx.cli.platform.handler.ComponentReader.LINE_HW_SW_FW;

public class ComponentStateReader implements CliOperReader<State, StateBuilder> {

    private static final String SH_MODULE_SINGLE = "sh module %s";

    private final Cli cli;

    public ComponentStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<State> instanceIdentifier,
                                      @Nonnull StateBuilder stateBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String name = instanceIdentifier.firstKeyOf(Component.class).getName();
        String output = blockingRead(f(SH_MODULE_SINGLE, name), cli, instanceIdentifier, readContext);
        parseFields(stateBuilder, name, output);
    }

    static void parseFields(@Nonnull StateBuilder stateBuilder, String name, String output) {
        String[] sections = ComponentReader.SEPARATOR.split(output);
        String cardLine = sections.length > 0 ? sections[0] : "";
        String hsSwFwLine = sections.length > 1 ? sections[1] : "";

        stateBuilder.setName(name);
        stateBuilder.setId(name);

        ParsingUtils.parseField(cardLine, 0,
                LINE::matcher,
                m -> m.group("type"),
                desc -> stateBuilder.setDescription(desc.trim()));

        ParsingUtils.parseField(cardLine, 0,
                LINE::matcher,
                m -> m.group("serial"),
                stateBuilder::setSerialNo);

        ParsingUtils.parseField(cardLine, 0,
                LINE::matcher,
                m -> m.group("model"),
                stateBuilder::setPartNo);

        ParsingUtils.parseField(hsSwFwLine, 0,
                LINE_HW_SW_FW::matcher,
                m -> m.group("hw"),
                stateBuilder::setVersion);

        // TODO We are reading just line cards now, so it should be fine
        // to always set LINECARD type for now. But in the future we should
        // take into account also other types
        stateBuilder.setType(new PlatformComponentState.Type(LINECARD.class));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull State state) {
        ((ComponentBuilder) builder).setState(state);
    }


}

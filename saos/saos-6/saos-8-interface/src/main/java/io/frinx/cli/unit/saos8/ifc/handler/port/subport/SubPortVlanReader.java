/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalElementsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalElementsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElements;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElementsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.elements.ClassElement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.elements.ClassElementBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanReader implements CliConfigReader<Vlan, VlanBuilder> {

    private static final String SUB_PORT_NAME_COMMAND = "configuration search string \"%s classifier-precedence %d \"";
    private static final String SH_ELEMENTS_COMMAND = "configuration search string \"sub-port %s class-element\"";

    private Cli cli;

    public SubPortVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @Nonnull VlanBuilder vlanBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String parentPort = instanceIdentifier.firstKeyOf(Interface.class).getName();
            Long index = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

            Optional<String> subPortName = getSubPortName(blockingRead(f(SUB_PORT_NAME_COMMAND, parentPort, index),
                    cli, instanceIdentifier, readContext));

            if (subPortName.isPresent()) {
                String output = blockingRead(f(SH_ELEMENTS_COMMAND, subPortName.get()),
                        cli, instanceIdentifier, readContext);
                parseVlan(output, vlanBuilder);
            }
        }
    }

    @VisibleForTesting
    void parseVlan(String output, VlanBuilder builder) {
        Saos8VlanLogicalElementsAugBuilder elementsAugBuilder = new Saos8VlanLogicalElementsAugBuilder();

        elementsAugBuilder.setClassElements(setVlanClassElements(output));

        if (elementsAugBuilder.getClassElements() != null) {
            builder.addAugmentation(Saos8VlanLogicalElementsAug.class, elementsAugBuilder.build()).build();
        }
    }

    private Optional<String> getSubPortName(String output) {
        Pattern namePattern = Pattern.compile(".* create sub-port (?<name>\\S+) .*");

        return ParsingUtils.parseField(output, 0,
            namePattern::matcher,
            matcher -> matcher.group("name"));
    }

    private ClassElements setVlanClassElements(String output) {
        ClassElementsBuilder classElementsBuilder = new ClassElementsBuilder();
        List<ClassElement> classElements = new ArrayList<>();

        Pattern allIdsPattern = Pattern.compile(".* class-element (?<id>\\d+) vtag-stack .*");

        List<String> allIds = ParsingUtils.parseFields(output, 0,
            allIdsPattern::matcher,
            matcher -> matcher.group("id"),
            id -> id);

        for (String id: allIds) {
            ClassElementBuilder elementBuilder = new ClassElementBuilder();
            elementBuilder.setId(id);

            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension
                    .elements._class.elements._class.element.ConfigBuilder elementConfigBuilder = new org.opendaylight
                    .yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements
                    ._class.elements._class.element.ConfigBuilder();

            Pattern vtagPattern = Pattern.compile(".* class-element " + id + " vtag-stack (?<vtag>\\S+)");

            elementConfigBuilder.setId(id);
            ParsingUtils.parseField(output, 0,
                vtagPattern::matcher,
                matcher -> matcher.group("vtag"),
                elementConfigBuilder::setVtagStack);

            elementBuilder.setConfig(elementConfigBuilder.build());
            classElements.add(elementBuilder.build());
        }

        return !classElements.isEmpty() ? classElementsBuilder.setClassElement(classElements).build() : null;
    }

    private boolean isPort(InstanceIdentifier<Vlan> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
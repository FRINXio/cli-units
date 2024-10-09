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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalElementsAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Saos8VlanLogicalElementsAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElement;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements.ClassElementBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.saos.vlan.logical.extension.elements._class.element.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortVlanReader implements CliConfigReader<Vlan, VlanBuilder> {

    private static final String SH_ELEMENTS_COMMAND = "configuration search string \"sub-port %s class-element\"";
    private static final Pattern CLASS_ELEMENTS_PAT = Pattern.compile(".* class-element (?<id>\\d+) "
            + "(vlan-untagged-data|vtag-stack).*");

    private Cli cli;

    public SubPortVlanReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Vlan> instanceIdentifier,
                                      @NotNull VlanBuilder vlanBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            var index = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();
            var subPortName = SubPortReader.findConfigInCache(new SubinterfaceKey(index), readContext).getName();
            final var output = blockingRead(f(SH_ELEMENTS_COMMAND, subPortName),
                    cli, instanceIdentifier, readContext);
            parseVlan(output, vlanBuilder);
        }
    }

    @VisibleForTesting
    static void parseVlan(String output, VlanBuilder builder) {
        var classElements = ParsingUtils.parseFields(output, 0,
            CLASS_ELEMENTS_PAT::matcher,
            matcher -> matcher.group("id"),
            id -> setClassElement(id, output));
        if (classElements != null && !classElements.isEmpty()) {
            builder.addAugmentation(Saos8VlanLogicalElementsAug.class, new Saos8VlanLogicalElementsAugBuilder()
                    .setClassElement(classElements)
                    .build())
                    .build();
        }
    }

    private static ClassElement setClassElement(final String id, final String output) {
        final var elementBuilder = new ClassElementBuilder().setId(id);
        final var configBuilder = new ConfigBuilder().setId(id);
        final var tagPattern = Pattern.compile(".* class-element " + id + " vtag-stack (?<vtag>\\S+)");
        final var untaggedPattern = Pattern.compile(".* class-element " + id + " vlan-untagged-data");
        ParsingUtils.parseField(output, 0,
            tagPattern::matcher,
            matcher -> matcher.group("vtag"),
            configBuilder::setVtagStack);
        configBuilder.setVlanUntaggedData(false);
        ParsingUtils.parseField(output, 0,
            untaggedPattern::matcher,
            matcher -> true,
            t -> configBuilder.setVlanUntaggedData(true));
        return elementBuilder.setConfig(configBuilder.build()).build();
    }

    private boolean isPort(InstanceIdentifier<Vlan> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
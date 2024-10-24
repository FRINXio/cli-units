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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAugBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.L2vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSICpuSubinterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    private final Cli cli;

    public L2VSICpuSubinterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        List<InterfaceKey> allIds = getAllIds(cli, this, instanceIdentifier, readContext, vsName);
        readContext.getModificationCache().put(getClass().getName(), Set.copyOf(allIds));
        return allIds;
    }

    @VisibleForTesting
    static List<InterfaceKey> getAllIds(Cli cli, CliReader cliReader,
                                        @NotNull InstanceIdentifier<?> id,
                                        @NotNull ReadContext readContext,
                                        String vsName) throws ReadFailedException {
        var output = cliReader.blockingRead(String.format(L2VSISubPortReader.SHOW_COMMAND, vsName), cli, id,
                readContext);
        var vrPattern = Pattern.compile("virtual-switch interface attach cpu-subinterface (?<name>\\S+)"
                + " vs " + vsName);
        return ParsingUtils.parseFields(output, 0,
            vrPattern::matcher,
            matcher -> matcher.group("name"),
            InterfaceKey::new);
    }


    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext readContext) {
        final var id = instanceIdentifier.firstKeyOf(Interface.class);

        final var subInterfaces =
                readContext.getModificationCache().containsKey(getClass().getName())
                        ? (Set<InterfaceKey>) readContext.getModificationCache().get(getClass().getName())
                        : Collections.emptySet();

        if (!subInterfaces.isEmpty() && subInterfaces.contains(id)) {
            interfaceBuilder
                    .setId(id.getId())
                    .setConfig(new ConfigBuilder()
                            .setId(id.getId())
                            .addAugmentation(Saos8NiIfcAug.class,
                                    new Saos8NiIfcAugBuilder().setType(L2vlan.class)
                                            .build())
                            .build());
        }
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
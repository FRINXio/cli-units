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
package io.frinx.cli.unit.saos8.ifc.handler.l2vlan;

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
import java.util.AbstractMap;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2VLANInterfaceReader implements
        CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {
    private static final String SHOW_COMMAND =
            "configuration search string \"cpu-interface sub-interface create cpu-subinterface\"";
    private static final Pattern ALL_IDS = Pattern.compile(".*sub-interface.*cpu-subinterface (?<name>\\S+).*");
    private Cli cli;

    public L2VLANInterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        return checkCachedIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    public static List<InterfaceKey> getAllIds(String output) {
        // we have to change the name for the l2vlan interfaces to prevent overlapping
        // in the database with the physical interfaces
        return ParsingUtils.parseFields(output, 0,
            ALL_IDS::matcher,
            m -> m.group("name"),
            value -> new InterfaceKey("cpu_subintf_" + value));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    public static List<InterfaceKey> checkCachedIds(Cli cli, CliReader reader,
                                                    @NotNull InstanceIdentifier id,
                                                    @NotNull ReadContext context) throws ReadFailedException {

        if (context.getModificationCache()
                .get(new AbstractMap.SimpleEntry<>(L2VLANInterfaceReader.class, reader)) != null) {
            return (List<InterfaceKey>) context.getModificationCache()
                    .get(new AbstractMap.SimpleEntry<>(L2VLANInterfaceReader.class, reader));
        }
        String output = reader.blockingRead(SHOW_COMMAND, cli, id, context);
        context.getModificationCache().put(
                new AbstractMap.SimpleEntry<>(L2VLANInterfaceReader.class, reader), getAllIds(output));
        return getAllIds(output);
    }
}
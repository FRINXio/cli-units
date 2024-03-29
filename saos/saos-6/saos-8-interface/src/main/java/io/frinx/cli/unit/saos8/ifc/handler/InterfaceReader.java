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

package io.frinx.cli.unit.saos8.ifc.handler;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    protected static final String SH_INTERFACE_SHOW = "interface show";
    protected static final Pattern TABLE_TYPE = Pattern.compile("\\+\\s+\\+");
    private static final Pattern INTERFACE_IDS = Pattern.compile("\\|\\s+(?<id>\\S+)\\s+\\|[\\s\\S]+");
    private final Cli cli;

    public InterfaceReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    @NotNull
    @Override
    public List<InterfaceKey> getAllIds(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                        @NotNull ReadContext readContext) throws ReadFailedException {
        return checkCachedIds(cli, this, instanceIdentifier, readContext);
    }

    protected static List<InterfaceKey> checkCachedIds(Cli cli, CliReader reader,
                                                     @NotNull InstanceIdentifier id,
                                                     @NotNull ReadContext context) throws ReadFailedException {

        if (context.getModificationCache().get(new AbstractMap.SimpleEntry<>(InterfaceReader.class, reader)) != null) {
            return (List<InterfaceKey>) context.getModificationCache()
                    .get(new AbstractMap.SimpleEntry<>(InterfaceReader.class, reader));
        }
        String output = reader.blockingRead(SH_INTERFACE_SHOW, cli, id, context);
        context.getModificationCache().put(
                new AbstractMap.SimpleEntry<>(InterfaceReader.class, reader), parseInterfaceIds(output));
        return parseInterfaceIds(output);
    }

    @VisibleForTesting
    public static List<InterfaceKey> parseInterfaceIds(String output) {
        return TABLE_TYPE.splitAsStream(output)
                .filter(line -> line.contains("INTERFACE MANAGEMENT") || line.contains("L3 INTERFACE"))
                .flatMap(ParsingUtils.NEWLINE::splitAsStream)
                .filter(line -> !line.contains("Name") && !line.contains("Admin"))
                .map(INTERFACE_IDS::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("id"))
                .distinct()
                .map(InterfaceKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder interfaceBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        interfaceBuilder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }
}
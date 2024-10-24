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

package io.frinx.cli.unit.saos8.ifc.handler.port;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_PORTS = "port show";
    private static final Pattern PORT_LINE = Pattern.compile("^\\|\\s(?<id>\\S+).*");

    public static final Check ETHERNET_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_ETHERNET_CSMACD);

    public static final Check LAG_CHECK = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_IEEE802AD_LAG);

    private Cli cli;

    public PortReader(Cli cli) {
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
        List<InterfaceKey> allIds = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(PORT_LINE::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group("id"))
                .filter(Objects::nonNull)
                .map(InterfaceKey::new)
                .distinct()
                .collect(Collectors.toList());
        return allIds.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Interface> instanceIdentifier,
                                      @NotNull InterfaceBuilder builder, @NotNull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }

    public static List<InterfaceKey> checkCachedIds(Cli cli, CliReader reader,
                                                    @NotNull InstanceIdentifier id,
                                                    @NotNull ReadContext context) throws ReadFailedException {

        if (context.getModificationCache().get(new AbstractMap.SimpleEntry<>(PortReader.class, reader)) != null) {
            return (List<InterfaceKey>) context.getModificationCache()
                    .get(new AbstractMap.SimpleEntry<>(PortReader.class, reader));
        }
        String output = reader.blockingRead(SH_PORTS, cli, id, context);
        context.getModificationCache().put(
                new AbstractMap.SimpleEntry<>(PortReader.class, reader), getAllIds(output));
        return getAllIds(output);
    }
}
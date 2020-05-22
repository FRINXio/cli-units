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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortReader implements CliConfigListReader<Interface, InterfaceKey, InterfaceBuilder>,
        CompositeListReader.Child<Interface, InterfaceKey, InterfaceBuilder> {

    public static final String SH_PORTS = "configuration search string \" port \"";
    public static final String LAG_PORTS = "configuration search string \"aggregation create\"";
    private static final Pattern INTERFACE_ID_LINE = Pattern.compile(".*port (?<id>\\S+).*");
    private static final Pattern LAG_INTERFACE_ID_LINE = Pattern.compile(".*agg (?<id>\\S+)");

    public static Check ethernetCheck = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_ETHERNET_CSMACD);

    public static Check lagCheck = BasicCheck.checkData(
            ChecksMap.DataCheck.InterfaceConfig.IID_TRANSFORMATION,
            ChecksMap.DataCheck.InterfaceConfig.TYPE_IEEE802AD_LAG);

    private Cli cli;

    public PortReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<InterfaceKey> getAllIds(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                        @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    public static List<InterfaceKey> getAllIds(Cli cli, CliReader reader,
                                               @Nonnull InstanceIdentifier id,
                                               @Nonnull ReadContext context) throws ReadFailedException {

        String portOutput = reader.blockingRead(SH_PORTS, cli, id, context);
        String lagOutput = reader.blockingRead(SH_PORTS, cli, id, context);

        List<Pattern> patterns = Arrays.asList(INTERFACE_ID_LINE, LAG_INTERFACE_ID_LINE);

        return ParsingUtils.NEWLINE.splitAsStream(portOutput.concat(lagOutput))
                .map(String::trim)
                .filter(l -> !l.startsWith("lldp"))
                .filter(l -> !l.startsWith("port tdm"))
                .map(line -> patterns.stream().map(pattern -> pattern.matcher(line))
                        .filter(Matcher::matches)
                        .map(matcher -> matcher.group("id"))
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(InterfaceKey::new)
                        .orElse(null))
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Interface> instanceIdentifier,
                                      @Nonnull InterfaceBuilder builder, @Nonnull ReadContext readContext) {
        builder.setName(instanceIdentifier.firstKeyOf(Interface.class).getName());
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}


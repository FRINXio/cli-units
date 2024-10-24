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
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private static final Pattern TYPE_IDS =
            Pattern.compile("\\|\\s+(?<id>\\S+)\\s+\\|\\s+(?<type>\\S+)\\s+\\|[\\s\\S]+");
    private final Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                      @NotNull final ConfigBuilder builder,
                                      @NotNull final ReadContext ctx) throws ReadFailedException {
        if (isInterface(id, ctx)) {
            String ifcName = id.firstKeyOf(Interface.class).getName();
            parseInterfaceConfig(blockingRead(InterfaceReader.SH_INTERFACE_SHOW, cli, id, ctx), builder, ifcName);
        }
    }

    @VisibleForTesting
    static void parseInterfaceConfig(final String output, final ConfigBuilder builder, String name) {
        builder.setName(name);
        parseType(output, builder, name);
    }

    private static void parseType(final String output, ConfigBuilder builder, String name) {
        InterfaceReader.TABLE_TYPE.splitAsStream(output)
                .filter(line -> line.contains("INTERFACE MANAGEMENT") || line.contains("L3 INTERFACE"))
                .flatMap(ParsingUtils.NEWLINE::splitAsStream)
                .filter(line -> line.contains(name))
                .map(TYPE_IDS::matcher)
                .filter(Matcher::matches)
                // we need to check interface name because the interface port (e.g. "1/2") can match with the ip address
                .map(m -> m.group("id").equals(name) ? m.group("type") : null)
                .filter(Objects::nonNull)
                .distinct()
                .findFirst()
                .ifPresent(type -> {
                    if (type.equals("loop")) {
                        builder.setType(SoftwareLoopback.class);
                    } else {
                        builder.setType(EthernetCsmacd.class);
                    }
                });
    }

    private boolean isInterface(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return InterfaceReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}
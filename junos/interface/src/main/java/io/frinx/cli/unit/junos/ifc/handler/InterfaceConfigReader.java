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

package io.frinx.cli.unit.junos.ifc.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Other;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_SINGLE_INTERFACE_CFG = "show configuration interfaces %s | display set";

    private static final Pattern SHUTDOWN_LINE = Pattern.compile("set interfaces (?<id>\\S+) disable");

    private static final Pattern DESCRIPTION_LINE = Pattern
        .compile("set interfaces (?<id>\\S+) description (?<desc>.*)");

    private Cli cli;

    public InterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull final Builder<? extends DataObject> builder, @Nonnull final Config value) {
        ((InterfaceBuilder) builder).setConfig(value);
    }

    @Override
    public void readCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
        @Nonnull final ConfigBuilder builder, @Nonnull final ReadContext ctx) throws ReadFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        parseInterface(blockingRead(String.format(SH_SINGLE_INTERFACE_CFG, name), cli, id, ctx), builder, name);
    }

    private static void parseInterface(final String output, final ConfigBuilder builder, final String name) {
        builder.setEnabled(true);
        builder.setName(name);
        builder.setType(parseType(name));

        // Actually check if disabled
        ParsingUtils
            .parseField(output, 0, SHUTDOWN_LINE::matcher, matcher -> false, builder::setEnabled);

        ParsingUtils
            .parseField(output, DESCRIPTION_LINE::matcher, matcher -> matcher.group("desc"),
                builder::setDescription);
    }

    static Class<? extends InterfaceType> parseType(final String name) {
        if (name.startsWith("et-") || name.startsWith("xe-") || name.startsWith("ge-") || name.startsWith("fe-")) {
            return EthernetCsmacd.class;
        } else {
            return Other.class;
        }
    }
}

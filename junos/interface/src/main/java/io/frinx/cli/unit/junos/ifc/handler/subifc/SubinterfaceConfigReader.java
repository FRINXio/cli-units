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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern SUBIF_DESCRIPTION_LINE = Pattern
        .compile("set interfaces (?<ifcId>\\S+) unit (?<subifcIndex>[0-9]+) description (?<desc>.*)");

    private static final Pattern SUBIF_DISABLE_LINE = Pattern
            .compile("set interfaces (?<ifcId>\\S+) unit (?<subifcIndex>[0-9]+) disable");

    private Cli cli;

    public SubinterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull ConfigBuilder builder,
        @Nonnull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        String subIfcName = SubinterfaceReader.getSubinterfaceName(id);
        String cmd = String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName);
        parseInterface(blockingRead(cmd, cli, id, ctx), builder, subKey.getIndex());
    }

    private static void parseInterface(final String output, final ConfigBuilder builder, Long subKey) {

        builder.setIndex(subKey);

        ParsingUtils
            .parseField(output, SUBIF_DESCRIPTION_LINE::matcher, matcher -> matcher.group("desc"),
                builder::setDescription);
        // "disable"
        builder.setEnabled(true);
        ParsingUtils
            .parseField(output, SUBIF_DISABLE_LINE::matcher, matcher -> false,
                builder::setEnabled);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((SubinterfaceBuilder) parentBuilder).setConfig(readValue);
    }
}

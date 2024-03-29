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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.ifc.Util;
import io.frinx.cli.unit.ios.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.SubinterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public SubinterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext ctx) throws ReadFailedException {
        SubinterfaceKey subKey = id.firstKeyOf(Subinterface.class);

        // Only parse configuration for non 0 subifc
        if (subKey.getIndex() == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            return;
        }

        String subIfcName = Util.getSubinterfaceName(id);
        String cmd = String.format(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, subIfcName);
        parseInterface(blockingRead(cmd, cli, id, ctx), builder, subKey.getIndex(), subIfcName);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder, Long subKey, String name) {
        // Set enabled unless proven otherwise
        builder.setEnabled(true);
        builder.setIndex(subKey);
        builder.setName(name);

        // Actually check if disabled
        ParsingUtils.parseField(output, 0,
            InterfaceConfigReader.SHUTDOWN_LINE::matcher,
            matcher -> false,
            builder::setEnabled);

        ParsingUtils.parseField(output,
            InterfaceConfigReader.DESCR_LINE::matcher,
            matcher -> matcher.group("desc"),
            builder::setDescription);
    }
}
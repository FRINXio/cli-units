/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.cfm;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.ifc.Util;
import io.frinx.cli.unit.iosxr.ifc.handler.InterfaceConfigReader;
import io.frinx.cli.unit.iosxr.ifc.handler.aggregate.AggregateConfigReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.ethernet.cfm._interface.cfm.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private final Cli cli;

    public CfmConfigReader(Cli cli) {
        this.cli = cli;
    }

    private static final Pattern CFM_ENABLED = Pattern.compile("ethernet cfm");

    @Override
    public void readCurrentAttributes(
        @NotNull InstanceIdentifier<Config> id,
        @NotNull ConfigBuilder builder,
        @NotNull ReadContext ctx) throws ReadFailedException {

        String ifcName = Util.getSubinterfaceName(id);
        if (!new AggregateConfigReader(cli).isLAGInterface(ifcName)) {
            // read cfm configuration just for LAG interfaces
            return;
        }

        String output = blockingRead(f(InterfaceConfigReader.SH_SINGLE_INTERFACE_CFG, ifcName), cli, id, ctx);
        parseCfmConfig(output, builder);
    }

    private static void parseCfmConfig(String output, ConfigBuilder builder) {
        ParsingUtils.findMatch(output, CFM_ENABLED, builder::setEnabled);
    }
}
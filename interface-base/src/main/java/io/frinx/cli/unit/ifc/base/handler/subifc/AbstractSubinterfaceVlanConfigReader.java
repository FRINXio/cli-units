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

package io.frinx.cli.unit.ifc.base.handler.subifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanLogicalConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public abstract class AbstractSubinterfaceVlanConfigReader implements CliConfigReader<Config, ConfigBuilder> {
    private final Cli cli;

    public AbstractSubinterfaceVlanConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                      @Nonnull ConfigBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        String subIfcName = getSubinterfaceName(id);

        String output = blockingRead(String.format(getReadCommand(), subIfcName),
                cli, id, ctx);
        parseVlanTag(output, builder, getVlanTagLine());
    }

    protected abstract String getSubinterfaceName(InstanceIdentifier<Config> instanceIdentifier);

    protected abstract Pattern getVlanTagLine();

    protected abstract String getReadCommand();

    @VisibleForTesting
    public static void parseVlanTag(String output, ConfigBuilder builder, Pattern vlanTagLine) {
        ParsingUtils.parseField(output,
            vlanTagLine::matcher,
            matcher -> matcher.group("tag"),
            tag -> builder.setVlanId(new VlanLogicalConfig.VlanId(new VlanId(Integer.valueOf(tag)))));
    }
}
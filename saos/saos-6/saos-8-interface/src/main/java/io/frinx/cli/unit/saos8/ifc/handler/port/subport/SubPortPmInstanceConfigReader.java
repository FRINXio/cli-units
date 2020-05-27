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

package io.frinx.cli.unit.saos8.ifc.handler.port.subport;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubPortPmInstanceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public SubPortPmInstanceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String pmInstanceName = instanceIdentifier.firstKeyOf(PmInstance.class).getName();
            String output = blockingRead(SubPortPmInstanceReader.SHOW_COMMAND, cli, instanceIdentifier, readContext);

            parsePmInstanceConfig(output, configBuilder, pmInstanceName);
        }
    }

    void parsePmInstanceConfig(String output, ConfigBuilder builder, String pmInstanceName) {
        builder.setName(pmInstanceName);
        setBinCount(output, builder, pmInstanceName);
    }

    private void setBinCount(String output, ConfigBuilder builder, String pmInstanceName) {
        Pattern pattern = Pattern.compile("pm create sub-port .*pm-instance " + pmInstanceName
                + " .* bin-count (?<bin>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("bin"),
            builder::setBinCount);
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.getAllIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
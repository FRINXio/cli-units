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

package io.frinx.cli.unit.saos8.ifc.handler.port.portqueuegroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos8.ifc.handler.port.PortReader;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.PmInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.pm.instances.pm.instances.pm.instance.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PortQueueGroupPmInstanceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private final Cli cli;

    public PortQueueGroupPmInstanceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        if (isPort(instanceIdentifier, readContext)) {
            String pmInstanceName = instanceIdentifier.firstKeyOf(PmInstance.class).getName();
            String output = blockingRead(PortQueueGroupPmInstanceReader.SHOW_COMMAND,
                    cli, instanceIdentifier, readContext);

            parsePmInstanceConfig(output, configBuilder, pmInstanceName);
        }
    }

    @VisibleForTesting
    static void parsePmInstanceConfig(String output, ConfigBuilder builder, String pmInstanceName) {
        builder.setName(pmInstanceName);

        setPortType(output, builder, pmInstanceName);
        setProfileType(output, builder, pmInstanceName);
        setBinCount(output, builder, pmInstanceName);
    }

    private static void setPortType(String output, ConfigBuilder builder, String pmInstanceName) {
        Pattern pattern = Pattern.compile("pm create (?<portType>\\S+).* .*pm-instance " + pmInstanceName
                + " .*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("portType"),
            builder::setPortType);
    }

    private static void setProfileType(String output, ConfigBuilder builder, String pmInstanceName) {
        Pattern pattern = Pattern.compile("pm create .*pm-instance " + pmInstanceName
                + " profile-type (?<profileType>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("profileType"),
            builder::setProfileType);
    }

    private static void setBinCount(String output, ConfigBuilder builder, String pmInstanceName) {
        Pattern pattern = Pattern.compile("pm create .*pm-instance " + pmInstanceName
                + " .* bin-count (?<bin>\\S+).*");

        ParsingUtils.parseField(output, 0,
            pattern::matcher,
            matcher -> matcher.group("bin"),
            builder::setBinCount);
    }

    private boolean isPort(InstanceIdentifier<Config> id, ReadContext readContext) throws ReadFailedException {
        return PortReader.checkCachedIds(cli, this, id, readContext)
                .contains(id.firstKeyOf(Interface.class));
    }
}
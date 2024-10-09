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

package io.frinx.cli.unit.saos.ifc.handler.l2cft;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceCftProfileConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND = "configuration search string \"port %s\"";

    private Cli cli;

    public InterfaceCftProfileConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        String output = blockingRead(f(SHOW_COMMAND, ifcName),
                cli, instanceIdentifier, readContext);
        parseConfig(output, ifcName, configBuilder);
    }

    @VisibleForTesting
    void parseConfig(String output, String ifcName, ConfigBuilder builder) {
        setProfileName(output, ifcName, builder);
        setEnabled(output, ifcName, builder);
    }

    private void setEnabled(String output, String ifcName, ConfigBuilder builder) {
        Pattern enabled = Pattern.compile("l2-cft enable port " + ifcName);

        ParsingUtils.parseField(output,
            enabled::matcher,
            matcher -> true,
            builder::setEnabled);
    }

    private void setProfileName(String output, String ifcName, ConfigBuilder builder) {
        Pattern profile = Pattern.compile("l2-cft set port " + ifcName + " profile (?<name>\\S+)");

        ParsingUtils.parseField(output,
            profile::matcher,
            matcher -> matcher.group("name"),
            builder::setName);
    }
}
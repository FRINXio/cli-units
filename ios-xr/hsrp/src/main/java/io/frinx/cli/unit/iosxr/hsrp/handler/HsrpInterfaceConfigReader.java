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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;

    public static final String SH_HSRP_INTERFACE = "show running-config router hsrp interface %s";
    private static final Pattern HSRP_DELAY_LINE =
            Pattern.compile("hsrp delay minimum (?<minDelay>[0-9]+) reload (?<relDelay>[0-9]+)");

    public HsrpInterfaceConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull ConfigBuilder configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId();
        configBuilder.setInterfaceId(interfaceId);
        String cmd = String.format(SH_HSRP_INTERFACE, interfaceId);
        parseInterface(blockingRead(cmd, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseInterface(final String output, final ConfigBuilder builder) {
        ParsingUtils.parseField(output, 0, HSRP_DELAY_LINE::matcher, matcher -> Long.valueOf(matcher.group("minDelay")),
                builder::setMinimumDelay);

        ParsingUtils.parseField(output, 0, HSRP_DELAY_LINE::matcher, matcher -> Long.valueOf(matcher.group("relDelay")),
                builder::setReloadDelay);
    }
}
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
package io.frinx.cli.unit.iosxe.cable.handler.rpd;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.CoreInterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.CoreInterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.core._interface.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdCoreInterfaceConfigReader implements CliConfigReader<CoreInterface, CoreInterfaceBuilder> {

    private static final Pattern INTERFACE_NAME_LINE =
            Pattern.compile("core-interface (?<name>[A-Za-z0-9]+\\/\\d\\/\\d)");
    private static final Pattern DELAY_LINE = Pattern.compile("network-delay (?<name>.+)");
    private static final Pattern PRINCIPAL_LINE = Pattern.compile("(?<name>principal)");

    private final Cli cli;

    public CableRpdCoreInterfaceConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<CoreInterface> instanceIdentifier,
                                      @Nonnull CoreInterfaceBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String rpdOutput = blockingRead(f(CableRpdConfigReader.SH_CABLE_RPD, rpdId),
                cli, instanceIdentifier, readContext);
        parseConfig(rpdOutput, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, CoreInterfaceBuilder configBuilder) {
        ConfigBuilder builder = new ConfigBuilder();
        builder.setPrincipal(Boolean.FALSE);

        ParsingUtils.parseField(output, 0,
            INTERFACE_NAME_LINE::matcher,
            matcher -> matcher.group("name"),
            builder::setName);

        ParsingUtils.parseField(output, 0,
            DELAY_LINE::matcher,
            matcher -> matcher.group("name"),
            builder::setNetworkDelay);

        ParsingUtils.parseField(output, 0,
            PRINCIPAL_LINE::matcher,
            matcher -> matcher.group("name"),
            setter -> builder.setPrincipal(Boolean.TRUE));

        configBuilder.setConfig(builder.build());
    }

}



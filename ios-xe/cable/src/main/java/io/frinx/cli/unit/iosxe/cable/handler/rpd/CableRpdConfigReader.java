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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.Rpd;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cable.rev211102.rpd.top.rpds.rpd.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CableRpdConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String SH_CABLE_RPD = "show running-config | section cable rpd %s";
    private static final Pattern DESCRIPTION_LINE = Pattern.compile(".*description (?<description>.+)");
    private static final Pattern IDENTIFIER_LINE = Pattern.compile(".*identifier (?<identifier>.+)");
    private static final Pattern RDTI_LINE = Pattern.compile(".*r-dti (?<rdti>.+)");
    private static final Pattern RPDEVENTPROFILE_LINE = Pattern.compile(".*rpd-event profile (?<rpdeventprofile>.+)");
    private static final Pattern RPDUSEVENT_LINE =
            Pattern.compile(".*rpd-55d1-us-event profile (?<rpd55d1useventprofile>.+)");
    private static final Pattern TYPE_LINE = Pattern.compile(".*type (?<type>.+)");

    private final Cli cli;

    public CableRpdConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        final String rpdId = instanceIdentifier.firstKeyOf(Rpd.class).getId();
        final String rpdOutput = blockingRead(f(SH_CABLE_RPD, rpdId),
                cli, instanceIdentifier, readContext);

        parseConfig(rpdOutput, configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, DESCRIPTION_LINE::matcher,
            matcher -> matcher.group("description"),
            configBuilder::setDescription);

        ParsingUtils.parseField(output, IDENTIFIER_LINE::matcher,
            matcher -> matcher.group("identifier"),
            configBuilder::setIdentifier);

        ParsingUtils.parseField(output, RDTI_LINE::matcher,
            matcher -> matcher.group("rdti"),
            configBuilder::setRDti);

        ParsingUtils.parseField(output, TYPE_LINE::matcher,
            matcher -> matcher.group("type"),
            configBuilder::setRpdType);

        ParsingUtils.parseField(output, RPDEVENTPROFILE_LINE::matcher,
            matcher -> matcher.group("rpdeventprofile"),
            configBuilder::setRpdEventProfile);

        ParsingUtils.parseField(output, RPDUSEVENT_LINE::matcher,
            matcher -> matcher.group("rpd55d1useventprofile"),
            configBuilder::setRpd55d1UsEventProfile);
    }
}
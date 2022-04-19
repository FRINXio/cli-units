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
package io.frinx.cli.unit.iosxe.system.handler.ntp;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.cisco.system.ntp._interface.source._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpInterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "ntp source {$sourceName} {$sourceNumber}\n"
            + "end";

    private static final String UPDATE_TEMPLATE = "configure terminal\n"
            + "{% if ($config.name) %}ntp source {$sourceName} {$sourceNumber}\n{% else %}"
            + "{% if ($before.name) %}no ntp source\n{% endif %}{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "{% if ($config.name) %}no ntp source\n{% endif %}"
            + "end";

    private final Cli cli;

    private static final Pattern SOURCE_NAME_OR_ID = Pattern.compile("(?<name>\\S+)(?<number>[0-9]+)");

    public NtpInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String sourceName = getSourceName(config.getName());
        String sourceNumber = getSourceNumber(config.getName());
        blockingWriteAndRead(cli, instanceIdentifier, config,
                fT(WRITE_TEMPLATE,
                    "sourceName", sourceName, "sourceNumber", sourceNumber));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String sourceName = getSourceName(dataAfter.getName());
        String sourceNumber = getSourceNumber(dataAfter.getName());
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                fT(UPDATE_TEMPLATE, "before", dataBefore, "sourceName", sourceName,
                    "sourceNumber", sourceNumber, "config", dataAfter));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, fT(DELETE_TEMPLATE, "config", config));
    }

    private static String getSourceName(String id) {
        return ParsingUtils.parseField(id, 0,
            SOURCE_NAME_OR_ID::matcher,
            matcher -> matcher.group("name")).orElse("");
    }

    private static String getSourceNumber(String id) {
        return ParsingUtils.parseField(id, 0,
            SOURCE_NAME_OR_ID::matcher,
            matcher -> matcher.group("number")).orElse("");
    }
}

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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.cisco.system.ntp.access.group.access.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.cisco.ntp.extension.rev220411.cisco.system.ntp.access.group.access.group.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NtpAccessGroupConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern ACCESS_PEER_LINE = Pattern.compile("ntp access-group peer (?<peer>\\d+).*");
    private static final Pattern ACCESS_SERVE_LINE = Pattern.compile("ntp access-group serve (?<serve>\\d+).*");
    private static final Pattern ACCESS_SERVE_ONLY_LINE =
            Pattern.compile("ntp access-group serve-only (?<sonly>\\d+).*");
    private static final Pattern ACCESS_QUERY_ONLY_LINE =
            Pattern.compile("ntp access-group query-only (?<qonly>\\d+).*");


    private static final String DISPLAY_NTP_ACCESS = "show running-config | include ntp access-group";

    private Cli cli;

    public NtpAccessGroupConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        parseConfig(blockingRead(DISPLAY_NTP_ACCESS, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseConfig(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseField(output, 0,
            ACCESS_PEER_LINE::matcher,
            matcher -> matcher.group("peer"),
            configBuilder::setPeer);

        ParsingUtils.parseField(output, 0,
            ACCESS_SERVE_LINE::matcher,
            matcher -> matcher.group("serve"),
            configBuilder::setServe);

        ParsingUtils.parseField(output, 0,
            ACCESS_SERVE_ONLY_LINE::matcher,
            matcher -> matcher.group("sonly"),
            configBuilder::setServeOnly);

        ParsingUtils.parseField(output, 0,
            ACCESS_QUERY_ONLY_LINE::matcher,
            matcher -> matcher.group("qonly"),
            configBuilder::setQueryOnly);
    }
}

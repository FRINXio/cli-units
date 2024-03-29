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

package io.frinx.cli.unit.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.global.config.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TeConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    @VisibleForTesting
    static final String SHOW_RUN_MPLS = "show running-config mpls traffic-eng";
    private static final Pattern MPLS_LINE = Pattern.compile("(.*)mpls traffic-eng(.*)");

    private Cli cli;

    public TeConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder builder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SHOW_RUN_MPLS, cli, instanceIdentifier, readContext);
        boolean isMplsTe = ParsingUtils.NEWLINE.splitAsStream(output)
                .map(String::trim)
                .map(MPLS_LINE::matcher)
                .anyMatch(Matcher::matches);
        if (isMplsTe) {
            builder.setEnabled(true);
        }
    }
}
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
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoadShareConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private Cli cli;
    private static final Pattern LOAD_LINE = Pattern.compile("load-share (?<load>.*)");

    public LoadShareConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull
            ConfigBuilder configBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final String name = instanceIdentifier.firstKeyOf(Tunnel.class)
                .getName();
        parseConfig(blockingRead(String.format(TunnelConfigReader.SH_RUN_TUNNEL, name), cli, instanceIdentifier,
                readContext), configBuilder);
    }

    @VisibleForTesting
    public static void parseConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, LOAD_LINE::matcher,
            matcher -> matcher.group("load"),
            v -> builder.setLoadShare(Long.valueOf(v)));
    }
}
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

package io.frinx.cli.unit.saos.qos.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.qos.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.qos.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosAugBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class QosConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SHOW_COMMAND =
            "configuration search string \"traffic-profiling\"";
    private static final Pattern ENABLE = Pattern.compile("traffic-profiling enable");

    private Cli cli;

    public QosConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        parseQosConfig(blockingRead(SHOW_COMMAND, cli, instanceIdentifier, readContext), configBuilder);
    }

    @VisibleForTesting
    static void parseQosConfig(String output, ConfigBuilder builder) {
        ParsingUtils.parseField(output, ENABLE::matcher, matcher -> true,
            enabled -> {
                SaosQosAugBuilder saosQosAugBuilder = new SaosQosAugBuilder().setEnabled(true);
                builder.addAugmentation(SaosQosAug.class, saosQosAugBuilder.build());
            });
    }
}
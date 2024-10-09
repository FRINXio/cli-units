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

package io.frinx.cli.unit.saos.broadcast.containment.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.Filters;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.FiltersBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentReader implements CliConfigReader<Filters, FiltersBuilder> {

    public static final String SH_BROADCAST_CONTAINMENT =
            "configuration search string \"broadcast-containment\"";

    private static final Pattern BC_ENABLE =
            Pattern.compile("(?<enable>broadcast-containment enable)?");

    private Cli cli;

    public BroadcastContainmentReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Filters> instanceIdentifier,
                                      @NotNull FiltersBuilder filtersBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_BROADCAST_CONTAINMENT, cli, instanceIdentifier, readContext);
        parseEnable(output, filtersBuilder);
    }

    @VisibleForTesting
    static void parseEnable(String output, FiltersBuilder filtersBuilder) {
        filtersBuilder.setEnabled(false);

        ParsingUtils.parseFields(output, 0,
            BC_ENABLE::matcher,
            m -> m.group("enable"),
            s -> filtersBuilder.setEnabled(true));
    }
}
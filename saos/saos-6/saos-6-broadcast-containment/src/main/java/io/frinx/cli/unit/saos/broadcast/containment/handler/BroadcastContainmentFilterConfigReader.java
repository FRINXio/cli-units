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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.ConfigBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final Pattern BC_FILTER_KBPS_LINE_PATTERN =
            Pattern.compile(".*kbps (?<kbps>\\d+).*");
    private static final Pattern BC_FILTER_CON_CLASS_LINE_PATTERN =
            Pattern.compile(".*containment-classification (?<conCas>.*)");

    private Cli cli;

    public BroadcastContainmentFilterConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        String outputForFilter = "configuration search running-config string \"broadcast-containment create filter "
                + filterName + "\"";
        String output = blockingRead(outputForFilter, cli, instanceIdentifier, readContext);
        parseAttributes(output, configBuilder);
    }

    @VisibleForTesting
    static void parseAttributes(String output, ConfigBuilder configBuilder) {
        ParsingUtils.parseFields(output, 0,
            BC_FILTER_KBPS_LINE_PATTERN::matcher,
            m -> m.group("kbps"),
            s -> configBuilder.setRate(BigInteger.valueOf(Integer.parseInt(s))));

        ParsingUtils.parseField(output, 0,
            BC_FILTER_CON_CLASS_LINE_PATTERN::matcher,
            m -> m.group("conCas"),
            s -> parseContainmentClassifications(s, configBuilder));
    }

    private static void parseContainmentClassifications(String output, ConfigBuilder configBuilder) {
        configBuilder.setContainmentClasification(Collections.emptyList());

        if (!output.isEmpty()) {
            String[] parts = output.split(",");
            configBuilder.setContainmentClasification(new ArrayList<>(Arrays.asList(parts)));
        }
    }
}

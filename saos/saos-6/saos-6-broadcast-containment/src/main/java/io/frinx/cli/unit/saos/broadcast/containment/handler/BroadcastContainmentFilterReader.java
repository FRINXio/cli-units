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
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.FilterBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.FilterKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterReader implements CliConfigListReader<Filter, FilterKey, FilterBuilder> {

    private static final Pattern BC_FILTER_LINE_PATTERN =
            Pattern.compile("broadcast-containment create filter (?<filter>\\S+).*");

    private Cli cli;

    public BroadcastContainmentFilterReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<FilterKey> getAllIds(@Nonnull InstanceIdentifier<Filter> instanceIdentifier,
                                     @Nonnull ReadContext readContext) throws ReadFailedException {
        return getAllIds(cli, this, instanceIdentifier, readContext);
    }

    @VisibleForTesting
    static List<FilterKey> getAllIds(Cli cli, CliReader cliReader,
                                              @Nonnull InstanceIdentifier<?> id,
                                              @Nonnull ReadContext readContext) throws ReadFailedException {

        String output = cliReader.blockingRead(BroadcastContainmentReader.SH_BROADCAST_CONTAINMENT,
                cli, id, readContext);
        return ParsingUtils.parseFields(output, 0,
            BC_FILTER_LINE_PATTERN::matcher,
            m -> m.group("filter"),
            FilterKey::new);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Filter> instanceIdentifier,
                                      @Nonnull FilterBuilder filterBuilder,
                                      @Nonnull ReadContext readContext) {
        filterBuilder.setName(instanceIdentifier.firstKeyOf(Filter.class).getName());
    }
}

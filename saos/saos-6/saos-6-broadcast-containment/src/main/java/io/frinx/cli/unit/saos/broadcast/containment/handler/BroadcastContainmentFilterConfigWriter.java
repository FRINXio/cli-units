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

import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.Filter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
                "{% if ($write) %}broadcast-containment create filter {$filter_name}\n{% endif %}"
                + "{% if ($listPresent) %}broadcast-containment set filter {$filter_name} kbps {$kbps}\n"
                + "broadcast-containment set filter {$filter_name} containment-classification {$containList}\n"
                + "{% else %}broadcast-containment set filter {$filter_name} kbps {$kbps}\n{% endif %}"
                + "configuration save";

    private static final String DELETE_TEMPLATE = "broadcast-containment delete filter {$filter_name}\n"
            + "configuration save";

    private Cli cli;

    public BroadcastContainmentFilterConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        String containList = parseContainList(Objects.requireNonNull(config.getContainmentClasification()));
        String rate = config.getRate() != null ? config.getRate().toString() : "0";
        Preconditions.checkArgument(config.getName().equals(filterName),
                "Filter names need to be equals");
        blockingWriteAndRead(cli, instanceIdentifier, config, getWriteTemplate(filterName, rate,
                containList, true, !containList.isEmpty()));
    }

    private String parseContainList(List<String> containmentClasification) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String part : containmentClasification) {
            stringBuilder.append(part);
            stringBuilder.append(",");
        }

        return stringBuilder.length() == 0 ? stringBuilder.toString()
                : stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    private String getWriteTemplate(String filterName, String kbps, String containList,
                                    Boolean write,
                                    Boolean listPresent) {
        return fT(WRITE_TEMPLATE, "filter_name", filterName, "kbps", kbps,
                "containList", containList, "write", write ? Chunk.TRUE : null,
                "listPresent" , listPresent ? Chunk.TRUE : null);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = id.firstKeyOf(Filter.class).getName();
        String containList = parseContainList(Objects.requireNonNull(dataAfter.getContainmentClasification()));
        String rate = dataAfter.getRate() != null ? dataAfter.getRate().toString() : "0";
        Preconditions.checkArgument(com.google.common.base.Objects.equal(com.google.common.base
                        .Objects.equal(dataAfter.getName(), dataBefore.getName()),
                com.google.common.base.Objects.equal(dataAfter.getName(), filterName)),
                "Filter names need to be equals");
        blockingWriteAndRead(cli, id, dataAfter, getWriteTemplate(filterName, rate,
                containList, false, !containList.isEmpty()));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String filterName = instanceIdentifier.firstKeyOf(Filter.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, getDeleteTemplate(filterName));
    }

    private String getDeleteTemplate(String filterName) {
        return fT(DELETE_TEMPLATE, "filter_name", filterName);
    }

}

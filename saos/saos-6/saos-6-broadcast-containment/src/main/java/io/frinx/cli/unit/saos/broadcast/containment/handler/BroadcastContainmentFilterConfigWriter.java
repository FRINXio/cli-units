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
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.broadcast.containment.rev200303.broadcast.containment.top.filters.filter.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BroadcastContainmentFilterConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "broadcast-containment create filter {$data.name}\n"
            + "{% if ($data.rate) %}broadcast-containment set filter {$data.name} kbps {$data.rate}\n{% endif %}"
            + "{% if ($containList) %}"
            + "broadcast-containment set filter {$data.name} containment-classification {$containList}\n"
            + "{% endif %}";

    private static final String UPDATE_TEMPLATE =
            "{% if ($rateCompare) %}"
            + "broadcast-containment set filter {$data.name} kbps {$data.rate}\n"
            + "{% endif %}"
            + "{% if ($classificationCompare) %}"
            + "broadcast-containment set filter {$data.name} containment-classification {$containList}\n"
            + "{% endif %}";

    private static final String DELETE_TEMPLATE = "broadcast-containment delete filter %s";

    private Cli cli;

    public BroadcastContainmentFilterConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, getWriteTemplate(config));
    }

    private String parseContainList(List<String> containmentClasification) {
        if (containmentClasification != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String part : containmentClasification) {
                stringBuilder.append(part);
                stringBuilder.append(",");
            }

            return stringBuilder.length() == 0 ? stringBuilder.toString()
                    : stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        }

        return null;
    }

    private String getWriteTemplate(Config config) {
        return fT(WRITE_TEMPLATE, "data", config,
                "containList", parseContainList(config.getContainmentClasification()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter, getUpdateTemplate(dataBefore, dataAfter));
    }

    private String getUpdateTemplate(Config dataBefore, Config dataAfter) {
        return fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore,
                "rateCompare", compareRate(dataBefore.getRate(), dataAfter.getRate()),
                "classificationCompare", compareClassifications(dataBefore.getContainmentClasification(),
                        dataAfter.getContainmentClasification()),
                "containList", parseContainList(dataAfter.getContainmentClasification()));
    }

    private String compareClassifications(List<String> before, List<String> after) {
        if (before != null) {
            Preconditions.checkNotNull(after,
                    "Containment classification parameter can not be empty.");

            Collections.sort(after);
            Collections.sort(before);
        }

        return Objects.equals(before, after) ? null : Chunk.TRUE;
    }

    private String compareRate(BigInteger before, BigInteger after) {
        if (before != null) {
            Preconditions.checkNotNull(after,
                    "Rate parameter can not be empty.");
        }
        return Objects.equals(before, after) ? null : Chunk.TRUE;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, f(DELETE_TEMPLATE, config.getName()));
    }
}
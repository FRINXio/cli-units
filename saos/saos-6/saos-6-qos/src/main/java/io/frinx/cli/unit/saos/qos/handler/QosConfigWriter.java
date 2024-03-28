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
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.top.qos.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class QosConfigWriter implements CliWriter<Config> {

    private static final String UPDATE_TEMPLATE = """
            {% if ($isEnabled) %}traffic-profiling enable
            {% else %}traffic-profiling disable
            {% endif %}""";

    private final Cli cli;

    public QosConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @VisibleForTesting
    static String writeTemplate(Config config) {
        SaosQosAug data = config.getAugmentation(SaosQosAug.class);
        return (data != null && data.isEnabled()) ? "traffic-profiling enable\n" : null;
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter) {
        SaosQosAug before = dataBefore.getAugmentation(SaosQosAug.class);
        SaosQosAug after = dataAfter.getAugmentation(SaosQosAug.class);
        if (after != null && after.isEnabled() != null) {
            if (before != null && before.isEnabled() != null && before.isEnabled().equals(after.isEnabled())) {
                return null;
            }
            return fT(UPDATE_TEMPLATE, "data", after, "before", before,
                    "isEnabled", after.isEnabled() ? Chunk.TRUE : null);
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, "traffic-profiling disable");
    }
}
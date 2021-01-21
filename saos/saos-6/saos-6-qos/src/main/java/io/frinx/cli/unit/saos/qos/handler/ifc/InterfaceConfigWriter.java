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

package io.frinx.cli.unit.saos.qos.handler.ifc;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosIfAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "{% if ($data.mode.name) %}traffic-profiling set port {$name} mode {$data.mode.name}\n{% endif %}"
            + "{% if ($isEnabled) %}traffic-profiling enable port {$name}\n{% endif %}"
            + "configuration save";

    private static final String UPDATE_TEMPLATE =
            "{% if ($mode) %}traffic-profiling set port {$name} mode {$mode}\n{% endif %}"
            + "{% if ($enabled == TRUE) %}traffic-profiling enable port {$name}\n"
            + "{% elseIf ($enabled == FALSE) %}traffic-profiling disable port {$name}\n{% endif %}"
            + "configuration save";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        SaosQosIfAug data = config.getAugmentation(SaosQosIfAug.class);
        if (data != null) {
            return fT(WRITE_TEMPLATE, "data", data,
                "name", config.getInterfaceId(),
                "isEnabled", (data.isEnabled() != null && data.isEnabled()) ? Chunk.TRUE : null);
        }
        return null;
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
    }

    @VisibleForTesting
    String updateTemplate(Config dataBefore, Config dataAfter) {
        SaosQosIfAug before = dataBefore.getAugmentation(SaosQosIfAug.class);
        SaosQosIfAug after = dataAfter.getAugmentation(SaosQosIfAug.class);

        if (after != null) {
            return fT(UPDATE_TEMPLATE, "data", after, "before", before,
                "name", dataAfter.getInterfaceId(),
                "mode", setMode(before, after),
                "enabled", setEnabled(before, after));
        }
        return null;
    }

    private String setEnabled(SaosQosIfAug before, SaosQosIfAug after) {
        if (after.isEnabled() != null) {
            if (before != null && before.isEnabled() != null && after.isEnabled().equals(before.isEnabled())) {
                return null;
            }
            return after.isEnabled() ? Chunk.TRUE : "FALSE";
        }
        return null;
    }

    private String setMode(SaosQosIfAug before, SaosQosIfAug after) {
        if (after.getMode() != null) {
            if (before != null && before.getMode() != null && after.getMode().equals(before.getMode())) {
                return null;
            }
            return after.getMode().getName();
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(config));
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        SaosQosIfAug data = config.getAugmentation(SaosQosIfAug.class);
        String template = "";
        if (data.getMode() != null) {
            template += f("traffic-profiling set port %s mode standard-dot1dpri\n",
                    config.getInterfaceId());
        }
        if (data.isEnabled() != null) {
            template += f("traffic-profiling disable port %s\n", config.getInterfaceId());
        }
        template += "configuration save";
        return template;
    }
}

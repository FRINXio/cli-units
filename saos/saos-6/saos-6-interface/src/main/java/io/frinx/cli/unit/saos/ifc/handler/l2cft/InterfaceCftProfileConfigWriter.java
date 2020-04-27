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

package io.frinx.cli.unit.saos.ifc.handler.l2cft;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.saos._if.extension.l2.cft.cft.profile.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceCftProfileConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE =
            "l2-cft set port {$ifcName} profile {$data.name}\n"
            + "{% if ($enabled == TRUE) %}l2-cft enable port {$ifcName}\n{% endif %}"
            + "configuration save";

    private static final String UPDATE_TEMPLATE =
            "{$data|update(name,l2-cft set port `$ifcName` profile `$data.name`\n,)}"
            + "{% if ($enabled == TRUE) %}l2-cft enable port {$ifcName}\n"
            + "{% elseIf ($enabled == FALSE) %}l2-cft disable port {$ifcName}\n"
            + "{% endif %}"
            + "configuration save";

    private static final String DELETE_TEMPLATE =
            "{% if ($enabled) %}l2-cft disable port {$ifcName}\n{% endif %}"
            + "l2-cft unset port {$ifcName} profile\n"
            + "configuration save";

    private Cli cli;

    public InterfaceCftProfileConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, ifcName));
    }

    @VisibleForTesting
    String writeTemplate(Config config, String ifcName) {
        return fT(WRITE_TEMPLATE, "data", config,
                "enabled", config.isEnabled(),
                "ifcName", ifcName);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, ifcName));
    }

    String updateTemplate(Config dataBefore, Config dataAfter, String ifcName) {
        return fT(UPDATE_TEMPLATE, "data", dataAfter, "before", dataBefore,
                "enabled", setEnabled(dataBefore, dataAfter),
                "ifcName", ifcName);
    }

    private String setEnabled(Config dataBefore, Config dataAfter) {
        Boolean before = dataBefore.isEnabled();
        Boolean after = dataAfter.isEnabled();

        if (!Objects.equals(before, after)) {
            if (before == null) {
                if (after) {
                    return Chunk.TRUE;
                }
            } else if (before) {
                return "FALSE";
            }
            if (after != null && after) {
                return Chunk.TRUE;
            }
        }
        return null;
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(config, ifcName));
    }

    @VisibleForTesting
    String deleteTemplate(Config config, String ifcName) {
        Boolean enabled = config.isEnabled();

        return fT(DELETE_TEMPLATE, "data", config,
                "enabled", (enabled == null || !enabled) ? null : Chunk.TRUE,
                "ifcName", ifcName);
    }
}

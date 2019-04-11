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

package io.frinx.cli.unit.junos.ifc.handler;

import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.ifc.base.handler.AbstractInterfaceConfigWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.Util;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String DELETE_TEMPLATE = "delete interfaces {$data.name}";

    private static final String WRITE_TEMPLATE = "{$data|update(description,"
            + "set interfaces `$data.name` description `$data.description`\n,"
            + "delete interfaces `$data.name` description\n)}"
            + "{% if ($enabled && $enabled == TRUE) %}delete interfaces {$data.name} disable"
            + "{% elseIf (!$enabled) %}set interfaces {$data.name} disable{% endif %}";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        // junos allows creating of physical interfaces
        blockingWriteAndRead(cli, id, data, updateTemplate(null, data));
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        // when "disable" is not set, "delete interface disable" will cause a error
        String enabled = "false";
        if (before != null && before.isEnabled() != null && !before.isEnabled()) {
            if (after.isEnabled() != null && after.isEnabled()) {
                enabled = Chunk.TRUE;
            }
        } else {
            if (after.isEnabled() != null && !after.isEnabled()) {
                enabled = null;
            }
        }
        return fT(WRITE_TEMPLATE, "before", before, "data", after, "enabled", enabled);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        // junos allows deleting of physical interfaces
        blockingDeleteAndRead(cli, id, deleteTemplate(dataBefore));
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }

    @Override
    public boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data);
    }
}

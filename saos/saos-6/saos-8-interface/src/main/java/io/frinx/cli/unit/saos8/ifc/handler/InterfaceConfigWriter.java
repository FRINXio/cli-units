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

package io.frinx.cli.unit.saos8.ifc.handler;

import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE_SAOS =
            // enable/disable port
            "{% if ($enabled) %}port enable port {$data.name}\n"
                    + "{% else %}port disable port {$data.name}\n{% endif %}"
                    // description
                    + "{% if ($desc) %}port set port {$data.name} description {$desc}\n"
                    + "{% else %}port unset port {$data.name} description\n{% endif %}"
                    // max-frame-size
                    + "{% if ($data.mtu) %}port set port {$data.name} max-frame-size {$data.mtu}\n"
                    + "{% else %}port set port {$data.name} max-frame-size 9216\n{% endif %}"
                    + "configuration save";

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new WriteFailedException.CreateFailedException(id, data,
                new IllegalArgumentException("Physical interface cannot be created"));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(dataBefore.getType()
                        .equals(dataAfter.getType()), "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new WriteFailedException.DeleteFailedException(id,
                new IllegalArgumentException("Physical interface cannot be deleted"));
    }

    private String updateTemplate(Config before, Config after) {
        return fT(WRITE_TEMPLATE_SAOS, "before", before,
                "data", after,
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
                "desc", getDescription(after));
    }

    private String getDescription(Config after) {
        if (after.getDescription() != null && after.getDescription().contains(" ")) {
            return String.format("\"%s\"", after.getDescription());
        }
        return after.getDescription();
    }
}
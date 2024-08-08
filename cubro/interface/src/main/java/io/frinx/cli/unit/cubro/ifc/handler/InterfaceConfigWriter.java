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

package io.frinx.cli.unit.cubro.ifc.handler;

import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.cubro.extension.rev200317.IfCubroAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE_CUBRO = """
            configure
            interface {$data.name}
            {% if ($enabled) %}shutdown
            {% else %}no shutdown
            {% endif %}{% if ($data.description) %}interface comment {$data.description}
            {% endif %}{% if ($data.mtu) %}mtu {$data.mtu}
            {% endif %}{% if ($rx) %}rx on\s
            {% endif %}{% if ($speed) %}speed {$speed}
            {% endif %}{% if ($innerhash) %}innerhash enable
            {% endif %}{% if ($inneracl) %}inneracl enable
            {% endif %}{% if ($vxlanterminated) %}vxlanterminated enable
            {% endif %}{% loop in $elags as $elag}elag {$elag}
            {% onEmpty %}{% endloop %}end
            """;

    private static final String DELETE_TEMPLATE_CUBRO = """
            configure
            interface {$data.name}
            no shutdown
            no mtu
            rx off
            no speed
            no innerhash enable
            no inneracl enable
            no vxlanterminated enable
            {% loop in $elags as $elag}no elag {$elag}
            {% onEmpty %}{% endloop %}end
            """;


    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, data, updateTemplate(null, data));

    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull Config dataAfter,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
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
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        IfCubroAug cubroAug = config.getAugmentation(IfCubroAug.class);
        String deleteTemp = fT(DELETE_TEMPLATE_CUBRO, "data", config,
                "elags", (cubroAug != null && cubroAug.getElag() != null) ? cubroAug.getElag() :
                        Collections.emptyList());

        blockingDeleteAndRead(cli, id, deleteTemp.trim());
    }

    private String updateTemplate(Config before, Config after) {
        IfCubroAug cubroAug = after.getAugmentation(IfCubroAug.class);
        String writeTemp;
        if (cubroAug != null) {
            writeTemp = fT(WRITE_TEMPLATE_CUBRO, "before", before, "data", after,
                    "enabled", (after.isEnabled() != null && !after.isEnabled()) ? Chunk.TRUE : null,
                    "rx", (cubroAug.isRx() != null && cubroAug.isRx()) ? Chunk.TRUE : null,
                    "speed", cubroAug.getSpeed(),
                    "innerhash", (cubroAug.isInnerhash() != null && cubroAug.isInnerhash()) ? Chunk.TRUE : null,
                    "inneracl", (cubroAug.isInneracl() != null && cubroAug.isInneracl()) ? Chunk.TRUE : null,
                    "vxlanterminated", (cubroAug.isVxlanterminated() != null && cubroAug.isVxlanterminated())
                            ? Chunk.TRUE : null,
                    "elags", (cubroAug.getElag() != null) ? cubroAug.getElag() : Collections.emptyList());
        } else {
            writeTemp = fT(WRITE_TEMPLATE_CUBRO, "before", before, "data", after,
                    "enabled", (after.isEnabled() != null && !after.isEnabled()) ? Chunk.TRUE : null);
        }
        return writeTemp.trim();
    }
}
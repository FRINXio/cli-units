/*
 * Copyright © 2022 Frinx and others.
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

package io.frinx.cli.unit.cer.ifc.handler;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.cer.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import java.util.Objects;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String WRITE_TEMPLATE_BASE = """
            configure
            interface {$data.name}
            {% if ($description) %}{$description}
            {% endif %}""";

    private static final String WRITE_TEMPLATE = WRITE_TEMPLATE_BASE
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "end";

    private static final String WRITE_TEMPLATE_RPD = WRITE_TEMPLATE_BASE
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "end";

    private static final String WRITE_TEMPLATE_CABLE_UPSTREAM = WRITE_TEMPLATE_BASE
            + "end";

    private static final String DELETE_TEMPLATE = """
            configure
            no interface {$data.name}
            end""";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        if (after.getName().contains("rpd")) {
            return fT(WRITE_TEMPLATE_RPD,
                    "before", before,
                    "data", after,
                    "description", updateDescription(before, after),
                    "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
        } else if (after.getName().contains("cable-upstream")) {
            return fT(WRITE_TEMPLATE_CABLE_UPSTREAM,
                    "before", before,
                    "data", after,
                    "description", updateDescription(before, after));
        } else {
            return fT(WRITE_TEMPLATE,
                    "before", before,
                    "data", after,
                    "description", updateDescription(before, after),
                    "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
        }
    }

    @Override
    protected boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data.getType());
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }

    private String updateDescription(Config dataBefore, Config dataAfter) {
        String descriptionBefore = dataBefore != null ? dataBefore.getDescription() : null;
        String descriptionAfter = dataAfter != null ? dataAfter.getDescription() : null;

        if (!Objects.equals(descriptionBefore, descriptionAfter)) {
            return descriptionAfter != null ? "description " + descriptionAfter : "no description";
        }

        return null;
    }
}


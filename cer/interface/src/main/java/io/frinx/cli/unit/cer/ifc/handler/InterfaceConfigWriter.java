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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String WRITE_TEMPLATE = "configure\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "end";

    private static final String WRITE_TEMPLATE_VLAN = "configure\n"
            + "interface {$data.name}\n"
            + "{% if ($data.mtu) %}mtu {$data.mtu}\n{% else %}no mtu\n{% endif %}"
            + "{% if ($data.description) %}description {$data.description}\n{% else %}no description\n{% endif %}"
            + "{% if ($enabled) %}no shutdown\n{% else %}shutdown\n{% endif %}"
            + "end";

    private static final String DELETE_TEMPLATE = "configure\n"
            + "no interface {$data.name}\n"
            + "end";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        if (isPhysicalInterface(after)) {
            return fT(WRITE_TEMPLATE,
                    "before", before,
                    "data", after,
                    "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
        }
        return fT(WRITE_TEMPLATE_VLAN,
                "before", before,
                "data", after,
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
    }

    @Override
    protected boolean isPhysicalInterface(Config data) {
        return Util.isPhysicalInterface(data.getType());
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }

}

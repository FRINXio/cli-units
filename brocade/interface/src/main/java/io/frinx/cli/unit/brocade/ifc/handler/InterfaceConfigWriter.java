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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface {$data.name}\n"
            + "{$data|update(mtu,mtu `$data.mtu`\n,no mtu\n)}"
            + "{$data|update(description,port-name `$data.description`\n,no port-name\n)}"
            + "{% if ($enabled) %}enable\n{% else %}disable\n{% endif %}"

            + "{% if ($priority) %}"
                + "{$priority|update(priority,priority `$priority.priority`\n,priority 0\n)}"
                + "{% if ($forced) %}priority force\n{% else %}no priority force\n{% endif %}"
            + "{% endif %}"

            + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no interface {$data.name}\n"
            + "end";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        IfBrocadePriorityAug priorityAug = after.getAugmentation(IfBrocadePriorityAug.class);
        return fT(WRITE_TEMPLATE, "before", before, "data", after,
            "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
            "priority", priorityAug, "forced", priorityAug.isPriorityForce() ? Chunk.TRUE : null);
    }

    @Override
    public boolean isPhysicalInterface(Config data) {
        return false;
    }

    @Override
    protected String deleteTemplate(Config data) {
        return fT(DELETE_TEMPLATE, "data", data);
    }
}

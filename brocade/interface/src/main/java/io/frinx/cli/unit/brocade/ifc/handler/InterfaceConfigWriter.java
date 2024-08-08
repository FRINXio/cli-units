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
import io.frinx.cli.unit.brocade.ifc.Util;
import io.frinx.cli.unit.ifc.base.handler.AbstractInterfaceConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.brocade.extension.rev190726.IfBrocadePriorityAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public final class InterfaceConfigWriter extends AbstractInterfaceConfigWriter {

    private static final String WRITE_TEMPLATE = """
            configure terminal
            interface {$data.name}
            {$data|update(mtu,mtu `$data.mtu`
            ,no mtu
            )}{$data|update(description,port-name `$data.description`
            ,no port-name
            )}{% if ($enabled) %}enable
            {% else %}disable
            {% endif %}{$priority}end""";

    private static final String PRIORITY_TEMPLATE =
            """
                    {$priority|update(priority,priority `$priority.priority`
                    ,priority 0
                    )}{% if ($update) %}{% if (!$forced) %}no {% endif %}priority force
                    {% endif %}""";


    private static final String DELETE_TEMPLATE = """
            configure terminal
            no interface {$data.name}
            end""";

    public InterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after) {
        boolean etheType = EthernetCsmacd.class.equals(Util.parseType(Util.expandInterfaceName(after.getName())));
        String priorityCommand = etheType ? getPriority(before, after) : "";

        return fT(WRITE_TEMPLATE, "before", before, "data", after,
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null,
                "priority", priorityCommand);
    }

    private String getPriority(Config before, Config after) {
        IfBrocadePriorityAug priorityAug = after.getAugmentation(IfBrocadePriorityAug.class);
        IfBrocadePriorityAug priorityAugBefore = null;
        if (before != null) {
            priorityAugBefore = before.getAugmentation(IfBrocadePriorityAug.class);
        }
        boolean update = !getPriorityForce(priorityAug).equals(getPriorityForce(priorityAugBefore));
        return fT(PRIORITY_TEMPLATE, "before", priorityAugBefore, "priority", priorityAug,
                "forced", (priorityAug != null && priorityAug.isPriorityForce() != null
                        && priorityAug.isPriorityForce()) ? Chunk.TRUE : null,
                "update", update ? Chunk.TRUE : null);
    }

    private Boolean getPriorityForce(IfBrocadePriorityAug priority) {
        return priority != null && priority.isPriorityForce() != null && priority.isPriorityForce();
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

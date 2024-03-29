/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.ifc.handler.subifc;

import com.x5.template.Chunk;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceConfigWriter;
import io.frinx.cli.unit.iosxe.ifc.Util;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceConfigWriter extends AbstractSubinterfaceConfigWriter {

    private static final String UPDATE_TEMPLATE = """
            configure terminal
            interface {$name}
            {% if ($data.description) %}description {$data.description}
            {% else %}no description
            {% endif %}{% if ($enabled) %}no shutdown
            {% else %}shutdown
            {% endif %}end""";

    private static final String DELETE_TEMPLATE = """
            configure terminal
            no interface {$name}
            end""";

    public SubinterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    protected String updateTemplate(Config before, Config after, InstanceIdentifier<Config> id) {
        return fT(UPDATE_TEMPLATE,
                "before", before,
                "data", after,
                "name", Util.getSubinterfaceName(id),
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
    }

    @Override
    protected String deleteTemplate(InstanceIdentifier<Config> id) {
        return fT(DELETE_TEMPLATE,"name", Util.getSubinterfaceName(id));
    }

}
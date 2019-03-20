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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.ifc.base.handler.subifc.AbstractSubinterfaceConfigWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.nexus.ifc.Util;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class SubinterfaceConfigWriter extends AbstractSubinterfaceConfigWriter {

    private static final String UPDATE_TEMPLATE = "interface {$name}\n"
            + "{$data|update(description,description `$data.description`\n,no description\n)}"
            //  + "{$data|update(is_enabled,shutdown\n,no shutdown\n}"
            + "{% if ($enabled) %}no shutdown{% else %}shutdown{% endif %}\n"
            + "root";

    private static final String DELETE_TEMPLATE = "no interface ${name}\n";

    public SubinterfaceConfigWriter(Cli cli) {
        super(cli);
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        // If we are creating new interface, allow empty config for .0 subifc
        if (!writeContext.readBefore(parentIfcId)
                .isPresent()
                && id.firstKeyOf(Subinterface.class)
                .getIndex() == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            Preconditions.checkArgument(data.getDescription() == null,
                    "'description' cannot be specified for .0 subinterface. "
                            + "Use 'description' under interface/config instead.");
            Preconditions.checkArgument(data.isEnabled() == null,
                    "'enabled' cannot be specified for .0 subinterface. "
                            + "Use 'enabled' under interface/config instead.");
            return;
        }
        if (isZeroSubinterface(id)) {
            // do nothing for .0 subinterface because it represents physical interface which config is handled in
            // interface/config
            return;
        }
        super.writeCurrentAttributes(id, data, writeContext);
    }

    @Override
    protected String updateTemplate(Config before, Config after, InstanceIdentifier<Config> id) {
        return fT(UPDATE_TEMPLATE, "before", before, "data", after, "name", Util.getSubinterfaceName(id),
                "enabled", (after.isEnabled() != null && after.isEnabled()) ? Chunk.TRUE : null);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config data,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        // if deleting parent interface allow deleting also .0 subifc
        if (!writeContext.readAfter(parentIfcId).isPresent()
                && id.firstKeyOf(Subinterface.class).getIndex() == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            return;
        }
        if (isZeroSubinterface(id)) {
            // do nothing for .0 subinterface because it represents physical interface which config is handled in
            // interface/config
            return;
        }
        super.deleteCurrentAttributes(id, data, writeContext);
    }

    @Override
    protected boolean isPhysicalInterface(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces
                                                      .rev161222.interfaces.top.interfaces._interface.Config data) {
        return Util.isPhysicalInterface(data);
    }

    @Override
    protected String deleteTemplate(InstanceIdentifier<Config> id) {
        return f(DELETE_TEMPLATE, "name", Util.getSubinterfaceName(id));
    }
}

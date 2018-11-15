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

package io.frinx.cli.unit.junos.ifc.handler.subifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.junos.ifc.handler.InterfaceConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public SubinterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
        @Nonnull WriteContext writeContext) throws WriteFailedException {
        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        Class<? extends InterfaceType> parentIfcType = writeContext.readAfter(parentIfcId).get().getConfig().getType();

        Preconditions.checkArgument(InterfaceConfigWriter.isSupportedType(parentIfcType),
            "Unable to create subinterface for interface of type %s", parentIfcType);

        String sbif = SubinterfaceReader.getSubinterfaceName(id);

        blockingWriteAndRead(cli, id, data,
            data.getDescription() == null
                ? f("delete interfaces %s description", sbif)
                : f("set interfaces %s description %s", sbif, data.getDescription()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);

        Class<? extends InterfaceType> parentIfcType = writeContext.readBefore(parentIfcId).get().getConfig().getType();

        Preconditions.checkArgument(InterfaceConfigWriter.isSupportedType(parentIfcType),
            "Unable to create subinterface for interface of type %s", parentIfcType);

        String sbif = SubinterfaceReader.getSubinterfaceName(id);

        blockingDeleteAndRead(cli, id, "delete interfaces " + sbif);
    }
}

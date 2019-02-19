/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.ifc.base.handler.subifc;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.subinterface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractSubinterfaceConfigWriter  implements CliWriter<Config> {

    private Cli cli;

    protected AbstractSubinterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces
                ._interface.Config parentData = writeContext.readAfter(parentIfcId).get().getConfig();

        if (isPhysicalInterface(parentData)) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Physical interface cannot be created"));
        }
        blockingWriteAndRead(cli, id, data, updateTemplate(null, data, id));
    }

    protected abstract String updateTemplate(Config before, Config after, InstanceIdentifier<Config> id);

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter, id));
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config data,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {

        InstanceIdentifier<Interface> parentIfcId = RWUtils.cutId(id, Interface.class);
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces
                ._interface.Config parentData = writeContext.readBefore(parentIfcId).get().getConfig();

        if (isPhysicalInterface(parentData)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        }
        blockingDeleteAndRead(cli, id, deleteTemplate(id));
    }

    protected abstract boolean isPhysicalInterface(
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces
                    ._interface.Config data);

    protected abstract String deleteTemplate(InstanceIdentifier<Config> id);


    protected static boolean isZeroSubinterface(@Nonnull InstanceIdentifier<?> id) {
        Long subifcIndex = id.firstKeyOf(Subinterface.class).getIndex();
        return subifcIndex == AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID;
    }
}

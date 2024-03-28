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

package io.frinx.cli.unit.ifc.base.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    protected AbstractInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(data)) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Physical interface cannot be created"));
        }

        if ((SoftwareLoopback.class.isAssignableFrom(data.getType())) && (data.getMtu() != null)) {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("MTU is not supported on Loopback interface!"));

        }

        blockingWriteAndRead(cli, id, data, updateTemplate(null, data));
    }

    protected abstract String updateTemplate(Config before, Config after);

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
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        }
        blockingDeleteAndRead(cli, id, deleteTemplate(dataBefore));
    }

    protected abstract String deleteTemplate(Config data);

    protected abstract boolean isPhysicalInterface(Config data);
}
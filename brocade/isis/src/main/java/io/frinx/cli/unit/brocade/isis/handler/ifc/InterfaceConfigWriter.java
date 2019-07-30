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

package io.frinx.cli.unit.brocade.isis.handler.ifc;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis._interface.group.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.isis.rev181121.isis.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfaceConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
        + "interface {$ifcName}\n"
        + "ip router isis\n"
        + "{% if (!$passive) %}no {% endif %}isis passive\n"
        + "end";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
        + "interface {$ifcName}\n"
        + "no ip router isis\n"
        + "end";

    private final Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingWriteAndRead(getWriteCommand(ifcName, config), cli, instanceIdentifier, config);
    }

    @VisibleForTesting
    String getWriteCommand(String ifcName, @Nonnull Config config) {
        return fT(WRITE_TEMPLATE, "ifcName", ifcName, "passive",
                config.isPassive() != null && config.isPassive() ? Chunk.TRUE : null);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore, @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getInterfaceId().getValue();
        blockingDeleteAndRead(fT(DELETE_TEMPLATE, "ifcName", ifcName), cli, instanceIdentifier);
    }
}

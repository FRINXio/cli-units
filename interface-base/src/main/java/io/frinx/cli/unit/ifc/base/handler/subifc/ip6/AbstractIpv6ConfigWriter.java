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

package io.frinx.cli.unit.ifc.base.handler.subifc.ip6;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv6.top.ipv6.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractIpv6ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AbstractIpv6ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                       @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config, ifcName));
    }

    protected abstract String writeTemplate(Config config, String ifcName);

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config before,
                                        @NotNull Config after,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        try {
            writeCurrentAttributes(instanceIdentifier, after, writeContext);
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(instanceIdentifier, before, after, e);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                        @NotNull Config config,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != AbstractSubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.DeleteFailedException(instanceIdentifier,
                    new IllegalArgumentException("Unable to manage IP for subinterface: " + subId));
        }
        blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(config, ifcName));
    }

    protected abstract String deleteTemplate(Config config, String ifcName);
}
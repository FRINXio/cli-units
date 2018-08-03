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

package io.frinx.cli.unit.huawei.ifc.handler.subifc.ip4;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.ifc.handler.subifc.SubinterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class Ipv4ConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public Ipv4ConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "system-view\n"
            + "interface %s\n"
            + "ip address %s %s\n"
            + "commit\n"
            + "return";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        if (subId != SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config, new
                    IllegalArgumentException("Unable to manage IP for subinterface: "
                    + subId));
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingWriteAndRead(cli, instanceIdentifier, config, f(WRITE_TEMPLATE, ifcName, config.getIp().getValue(),
                config.getPrefixLength()));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            before, @Nonnull Config after, @Nonnull WriteContext writeContext) throws WriteFailedException {
        try {
            deleteCurrentAttributes(instanceIdentifier, before, writeContext);
            writeCurrentAttributes(instanceIdentifier, after, writeContext);
        } catch (WriteFailedException e) {
            throw new WriteFailedException.UpdateFailedException(instanceIdentifier, before, after, e);
        }
    }

    private static final String DELETE_TEMPLATE = "system-view\n"
            + "interface %s\n"
            + "undo ip address\n"
            + "commit\n"
            + "return";

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = instanceIdentifier.firstKeyOf(Subinterface.class).getIndex();

        // TODO Probably not needed since we do not support IP configuraion
        // on any other subinterface
        if (subId != SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.DeleteFailedException(instanceIdentifier, new IllegalArgumentException(
                    "Unable to manage IP for subinterface: "
                + subId));
        }

        String ifcName = instanceIdentifier.firstKeyOf(Interface.class).getName();

        blockingDeleteAndRead(f(DELETE_TEMPLATE, ifcName), cli, instanceIdentifier);
    }
}

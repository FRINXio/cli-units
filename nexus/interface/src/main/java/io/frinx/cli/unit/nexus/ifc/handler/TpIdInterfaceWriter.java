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

package io.frinx.cli.unit.nexus.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TpIdInterfaceWriter implements CliWriter<Config1> {

    private Cli cli;

    public TpIdInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                       @Nonnull Config1 data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {

        String name = id.firstKeyOf(Interface.class).getName();
        String tpIdForDevice = getTpIdForDevice(data);

        blockingWriteAndRead(cli, id, data,
                f("interface %s", name),
                data.getTpid() == null ? "no switchport dot1q ethertype" : f("switchport\n" + "switchport dot1q "
                        + "ethertype %s", tpIdForDevice),
                "root");

    }

    private static String getTpIdForDevice(@Nonnull Config1 dataAfter) {
        return dataAfter.getTpid()
                .getSimpleName()
                .toLowerCase();

    }

    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id, @Nonnull Config1 dataBefore,
                                        @Nonnull Config1 dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        if (dataAfter.getTpid() == null) {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id, @Nonnull Config1
            config1, @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, id,
                f("interface %s", ifcName),
                f("no switchport dot1q ethertype"),
                "root");

    }
}

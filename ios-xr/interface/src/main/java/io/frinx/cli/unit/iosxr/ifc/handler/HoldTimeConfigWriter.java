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

package io.frinx.cli.unit.iosxr.ifc.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222._interface.phys.holdtime.top.hold.time.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class HoldTimeConfigWriter implements CliWriter<Config> {

    static final String WRITE_CURR_ATTR = "interface {$ifcName}\n" +
            //About this i was not sure. command template looked strange.
            // There was 2 variables but nothing was sending any string into that variables.
            //So i used String variables up and down which was the only one's which was defined and not used.
            "carrier-delay {$up} {$down}\n" +
            "exit";

    static final String DELETE_CURR_ATTR = "interface {$ifcName}\n" +
            "no carrier-delay\n" +
            "exit";

    private Cli cli;

    public HoldTimeConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {

        String ifcName = id.firstKeyOf(Interface.class).getName();

        String up = dataAfter.getUp() == null ? "" : f("up %s", dataAfter.getUp());
        String down = dataAfter.getDown() == null ? "" : f("down %s", dataAfter.getDown());

        // TODO We should restrict this probably just to physical ifcs
        blockingWriteAndRead(cli, id, dataAfter, fT(WRITE_CURR_ATTR,
                "ifcName", ifcName,
                "up", up,
                "down", down));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {
        if (dataAfter.getDown() == null && dataAfter.getUp() == null) {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String ifcName = id.firstKeyOf(Interface.class).getName();
        blockingDeleteAndRead(cli, id, fT(DELETE_CURR_ATTR,
                "ifcName", ifcName));
    }
}

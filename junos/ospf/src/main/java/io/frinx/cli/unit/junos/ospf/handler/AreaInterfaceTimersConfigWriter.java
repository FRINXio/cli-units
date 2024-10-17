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

package io.frinx.cli.unit.junos.ospf.handler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.timers.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceTimersConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceTimersConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class))
                        .get()
                        .getKey();
        if (data.getRetransmissionInterval() != null) {
            String cmd = f("set protocols ospf area %s interface %s retransmit-interval %s",
                    AreaInterfaceReader.areaIdToString(areaId), intfId.getId(), data.getRetransmissionInterval());
            blockingWriteAndRead(cli, instanceIdentifier, data, cmd);
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(id, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(id, Interface.class)).get().getKey();

        if (dataAfter.getRetransmissionInterval() != null) {
            String cmd = f("set protocols ospf area %s interface %s retransmit-interval %s",
                    AreaInterfaceReader.areaIdToString(areaId), intfId.getId(), dataAfter.getRetransmissionInterval());
            blockingWriteAndRead(cli, id, dataAfter, cmd);
        } else if (dataBefore.getRetransmissionInterval() != null) {
            String delcmd = f("delete protocols ospf area %s interface %s retransmit-interval",
                    AreaInterfaceReader.areaIdToString(areaId), intfId.getId());
            blockingWriteAndRead(cli, id, dataBefore, delcmd);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
            @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Interface.class))
                        .get()
                        .getKey();
        blockingDeleteAndRead(cli, instanceIdentifier,
                f("delete protocols ospf area %s interface %s retransmit-interval",
                        AreaInterfaceReader.areaIdToString(areaId), intfId.getId()));
    }
}
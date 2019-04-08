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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.bfd.rev171024.bfd.top.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceBfdConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceBfdConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class))
                        .get()
                        .getKey();
        String cmd = f("set%s protocols ospf area %s interface %s bfd-liveness-detection",
                OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());

        if (data.getMinInterval() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, data,
                    f("%s minimum-interval %s", cmd, data.getMinInterval()));
        }

        if (data.getMinReceiveInterval() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, data,
                    f("%s minimum-receive-interval %s", cmd, data.getMinReceiveInterval()));
        }

        if (data.getMultiplier() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, data,
                    f("%s multiplier %s", cmd, data.getMultiplier()));
        }
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(id, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(id, Interface.class)).get().getKey();

        String cmd = f("set%s protocols ospf area %s interface %s bfd-liveness-detection",
                OspfProtocolReader.resolveVrfWithName(id),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());
        String delcmd = f("delete%s protocols ospf area %s interface %s bfd-liveness-detection",
                OspfProtocolReader.resolveVrfWithName(id),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());

        if (dataAfter.getMinInterval() != null) {
            blockingWriteAndRead(cli, id, dataAfter, f("%s minimum-interval %s", cmd, dataAfter.getMinInterval()));
        } else if (dataBefore.getMinInterval() != null) {
            blockingWriteAndRead(cli, id, dataBefore, f("%s minimum-interval", delcmd));
        }

        if (dataAfter.getMinReceiveInterval() != null) {
            blockingWriteAndRead(cli, id, dataAfter,
                    f("%s minimum-receive-interval %s", cmd, dataAfter.getMinReceiveInterval()));
        } else if (dataBefore.getMinReceiveInterval() != null) {
            blockingWriteAndRead(cli, id, dataBefore, f("%s minimum-receive-interval", delcmd));
        }

        if (dataAfter.getMultiplier() != null) {
            blockingWriteAndRead(cli, id, dataAfter, f("%s multiplier %s", cmd, dataAfter.getMultiplier()));
        } else if (dataBefore.getMultiplier() != null) {
            blockingWriteAndRead(cli, id, dataBefore, f("%s multiplier", delcmd));
        }
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Interface.class))
                        .get()
                        .getKey();
        blockingDeleteAndRead(cli, instanceIdentifier,
                f("delete%s protocols ospf area %s interface %s bfd-liveness-detection",
                        OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                        AreaInterfaceReader.areaIdToString(areaId), intfId.getId()));
    }
}

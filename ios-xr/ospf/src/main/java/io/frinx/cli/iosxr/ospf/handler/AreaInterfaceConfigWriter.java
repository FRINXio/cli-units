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

package io.frinx.cli.iosxr.ospf.handler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AreaInterfaceConfigWriter implements OspfWriter<Config> {

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                data.getMetric() != null ? f("cost %s", data.getMetric().getValue()) : "no cost",
                "exit",
                "exit",
                "exit");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config dataBefore, Config dataAfter, WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(instanceIdentifier, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s", instanceIdentifier.firstKeyOf(Protocol.class).getName()),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("no interface %s", intfId.getId()),
                "exit",
                "exit");
    }
}

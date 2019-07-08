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

package io.frinx.cli.unit.iosxr.ospf.handler;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bfd.rev171117.bfd.enable.enable.bfd.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceEnableBfdConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceEnableBfdConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId = instanceIdentifier.firstKeyOf(Interface.class);
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s %s", instanceIdentifier.firstKeyOf(Protocol.class)
                        .getName(), OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                data.isEnabled() != null ? (data.isEnabled() ? "bfd fast-detect" : "bfd fast-detect disable") : "",
                "root");
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(id, Area.class))
                        .get()
                        .getIdentifier();
        final InterfaceKey intfId = id.firstKeyOf(Interface.class);
        blockingWriteAndRead(cli, id, dataAfter,
                f("router ospf %s %s", id.firstKeyOf(Protocol.class)
                        .getName(), OspfProtocolReader.resolveVrfWithName(id)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                dataAfter.isEnabled() != null ? (dataAfter.isEnabled() ? "bfd fast-detect" : "bfd fast-detect "
                        + "disable") : "no bfd fast-detect",
                "root");
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId = instanceIdentifier.firstKeyOf(Area.class)
                .getIdentifier();
        final InterfaceKey intfId = instanceIdentifier.firstKeyOf(Interface.class);
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s %s", instanceIdentifier.firstKeyOf(Protocol.class)
                        .getName(), OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", intfId.getId()),
                "no bfd fast-detect",
                "root");
    }
}

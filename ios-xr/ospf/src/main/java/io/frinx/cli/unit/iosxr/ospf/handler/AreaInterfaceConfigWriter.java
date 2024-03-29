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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class AreaInterfaceConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        blockingWriteAndRead(cli, instanceIdentifier, data,
                f("router ospf %s %s", instanceIdentifier.firstKeyOf(Protocol.class)
                        .getName(), OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", data.getId()),
                data.getMetric() != null ? f("cost %s", data.getMetric()
                        .getValue()) : "",
                data.isPassive() != null ? data.isPassive() ? "passive enable" : "passive disable" : "",
                "root");
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
            @NotNull Config dataBefore, @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws
            WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                f("router ospf %s %s", instanceIdentifier.firstKeyOf(Protocol.class).getName(),
                        OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("interface %s", dataAfter.getId()),
                dataAfter.getMetric() != null ? f("cost %s", dataAfter.getMetric().getValue())
                        : dataBefore.getMetric() != null ? "no cost" : "",
                dataAfter.isPassive() != null ? dataAfter.isPassive() == false ? "passive disable" : "passive enable" :
                    dataBefore.isPassive() != null ? "no passive" : "",
                "root");
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier, @NotNull Config data,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readBefore(RWUtils.cutId(instanceIdentifier, Area.class))
                        .get()
                        .getIdentifier();
        blockingDeleteAndRead(cli, instanceIdentifier,
                f("router ospf %s %s", instanceIdentifier.firstKeyOf(Protocol.class)
                        .getName(), OspfProtocolReader.resolveVrfWithName(instanceIdentifier)),
                f("area %s", AreaInterfaceReader.areaIdToString(areaId)),
                f("no interface %s", data.getId()),
                "root");
    }
}
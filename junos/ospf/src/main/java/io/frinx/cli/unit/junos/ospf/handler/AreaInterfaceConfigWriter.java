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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.OspfAreaIfConfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OSPFNETWORKTYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfAreaIdentifier;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.POINTTOPOINTNETWORK;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class AreaInterfaceConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public AreaInterfaceConfigWriter(final Cli cli) {
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
        String cmd = f("set%s protocols ospf area %s interface %s",
                OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());
        blockingWriteAndRead(cli, instanceIdentifier, data, cmd);

        if (data.getAugmentation(OspfAreaIfConfAug.class).isEnabled()) {
            String delcmd = f("delete%s protocols ospf area %s interface %s",
                    OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                    AreaInterfaceReader.areaIdToString(areaId), intfId.getId());
            blockingWriteAndRead(cli, instanceIdentifier, data, f("%s disable", delcmd));
        } else {
            blockingWriteAndRead(cli, instanceIdentifier, data, f("%s disable", cmd));
        }

        if (data.getNetworkType() != null) {

            Preconditions.checkArgument(parseNwType(data.getNetworkType()) != null,
                    "Unknown Network Type is specified %s.", data.getNetworkType());
            blockingWriteAndRead(cli, instanceIdentifier, data,
                    f("%s interface-type %s", cmd, parseNwType(data.getNetworkType())));
        }

        if (data.getMetric() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, data,
                    f("%s metric %s", cmd, data.getMetric().getValue()));
        }

        if (data.getPriority() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, data, f("%s priority %s", cmd, data.getPriority()));
        }
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> instanceIdentifier, Config dataBefore,
                                               Config dataAfter, WriteContext writeContext) throws
            WriteFailedException {
        final OspfAreaIdentifier areaId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Area.class)).get().getIdentifier();
        final InterfaceKey intfId =
                writeContext.readAfter(RWUtils.cutId(instanceIdentifier, Interface.class)).get().getKey();

        String cmd = f("set%s protocols ospf area %s interface %s",
                OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());
        String delcmd = f("delete%s protocols ospf area %s interface %s",
                OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                AreaInterfaceReader.areaIdToString(areaId), intfId.getId());

        if (dataAfter.getAugmentation(OspfAreaIfConfAug.class).isEnabled()) {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter, f("%s disable", delcmd));
        } else {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter, f("%s disable", cmd));
        }

        if (dataAfter.getNetworkType() != null) {
            Preconditions.checkArgument(parseNwType(dataAfter.getNetworkType()) != null,
                     "Unknown Network Type is specified %s.", dataAfter.getNetworkType());
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                    f("%s interface-type %s", cmd, parseNwType(dataAfter.getNetworkType())));
        } else if (dataBefore.getNetworkType() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, dataBefore,
                    f("%s interface-type", delcmd));
        }

        if (dataAfter.getMetric() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                        f("%s metric %s", cmd, dataAfter.getMetric().getValue()));
        } else if (dataBefore.getMetric() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, dataBefore, f("%s metric", delcmd));
        }

        if (dataAfter.getPriority() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, dataAfter,
                        f("%s priority %s", cmd, dataAfter.getPriority()));
        } else if (dataBefore.getPriority() != null) {
            blockingWriteAndRead(cli, instanceIdentifier, dataBefore, f("%s priority", delcmd));
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
                f("delete%s protocols ospf area %s interface %s",
                        OspfProtocolReader.resolveVrfWithName(instanceIdentifier),
                        AreaInterfaceReader.areaIdToString(areaId), intfId.getId()));
    }

    private static String parseNwType(final Class<? extends OSPFNETWORKTYPE> name) {
        if (name == POINTTOPOINTNETWORK.class) {
            return "p2p";
        } else {
            return null;
        }
    }
}

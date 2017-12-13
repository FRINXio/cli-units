/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.brocade.ifc.handler;

import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getIfcNumber;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.getTypeOnDevice;
import static io.frinx.cli.unit.brocade.ifc.handler.InterfaceConfigReader.parseType;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.TPID0X8A88;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class TpIdInterfaceWriter implements CliWriter<Config1> {

    private Cli cli;

    public TpIdInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                       @Nonnull Config1 dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String typeOnDevice = getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);

        String tpIdForDevice = getTpIdForDevice(dataAfter);

        blockingWriteAndRead(cli, id, dataAfter,
                "configure terminal",
                f("tag-type %s %s %s", tpIdForDevice, typeOnDevice, ifcNumber),
                "end");
    }

    private static String getTpIdForDevice(@Nonnull Config1 dataAfter) {
        if (dataAfter.getTpid() == TPID0X8A88.class) {
            return "88A8";
        } else {
            String simpleTpIdClassName = dataAfter.getTpid().getSimpleName().toLowerCase();
            return simpleTpIdClassName.substring(simpleTpIdClassName.indexOf('x') + 1).toUpperCase();
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config1> id,
                                        @Nonnull Config1 dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        String name = id.firstKeyOf(Interface.class).getName();
        Class<? extends InterfaceType> ifcType = parseType(name);
        String typeOnDevice = getTypeOnDevice(ifcType);
        String ifcNumber = getIfcNumber(name);

        String tpIdForDevice = getTpIdForDevice(dataBefore);

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("tag-type %s %s %s", tpIdForDevice, typeOnDevice, ifcNumber),
                "end");
    }
}

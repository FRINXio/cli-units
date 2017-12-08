/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import static io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.ios.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceVlanConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public SubinterfaceVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String WRITE_TEMPLATE = "configure terminal\n" +
            "interface %s\n" +
            "encapsulation dot1Q %s\n" +
            "exit\n" +
            "exit";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        if (subId == ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(id, dataAfter,
                    new IllegalArgumentException("Unable to manage Vlan for subinterface: " + ZERO_SUBINTERFACE_ID));
        } else {
            blockingWriteAndRead(cli, id, dataAfter,
                    f(WRITE_TEMPLATE,
                            getSubinterfaceName(id),
                            dataAfter.getVlanId().getVlanId().getValue()));
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    private static final String DELETE_TEMPLATE = "configure terminal\n" +
            "interface %s\n" +
            "no encapsulation dot1Q %s\n" +
            "exit\n" +
            "exit";


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        if (subId == ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unable to manage Vlan for subinterface: " + ZERO_SUBINTERFACE_ID));
        } else {
            blockingDeleteAndRead(cli, id,
                    f(DELETE_TEMPLATE,
                            getSubinterfaceName(id),
                            dataBefore.getVlanId().getVlanId().getValue()));
        }
    }
}

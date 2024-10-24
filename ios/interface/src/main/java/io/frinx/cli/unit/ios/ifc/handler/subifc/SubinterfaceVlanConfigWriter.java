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

package io.frinx.cli.unit.ios.ifc.handler.subifc;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.ios.ifc.Util;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceVlanConfigWriter extends AbstractSubinterfaceVlanConfigWriter {
    private static final String WRITE_TEMPLATE =
            """
                    configure terminal
                    interface %s
                    encapsulation dot1Q %s
                    end""";
    private static final String DELETE_TEMPLATE =
            """
                    configure terminal
                    interface %s
                    no encapsulation dot1Q %s
                    end""";

    private final Cli cli;

    public SubinterfaceVlanConfigWriter(Cli cli) {
        super(cli);
        this.cli = cli;
    }

    @Override
    protected String getSubinterfaceName(InstanceIdentifier<Config> instanceIdentifier) {
        return Util.getSubinterfaceName(instanceIdentifier);
    }

    @Override
    protected String getWriteTemplate() {
        return WRITE_TEMPLATE;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                       @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(id, dataAfter,
                    new IllegalArgumentException("Unable to manage Vlan for subinterface: "
                            + SubinterfaceReader.ZERO_SUBINTERFACE_ID));
        } else {
            super.writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id,
                                        @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        Long subId = id.firstKeyOf(Subinterface.class).getIndex();

        if (subId == SubinterfaceReader.ZERO_SUBINTERFACE_ID) {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unable to manage Vlan for subinterface: "
                            + SubinterfaceReader.ZERO_SUBINTERFACE_ID));
        } else {
            blockingDeleteAndRead(cli, id,
                    f(DELETE_TEMPLATE,
                            Util.getSubinterfaceName(id),
                            dataBefore.getVlanId().getVlanId().getValue()));
        }
    }
}
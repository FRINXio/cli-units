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

package io.frinx.cli.unit.nexus.ifc.handler.subifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.AbstractSubinterfaceVlanConfigWriter;
import io.frinx.cli.unit.nexus.ifc.Util;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class SubinterfaceVlanConfigWriter extends AbstractSubinterfaceVlanConfigWriter {
    private static final String WRITE_TEMPLATE = "interface %s\n" + "encapsulation dot1q %s\n" + "root";
    private static final String DELETE_TEMPLATE = "interface %s\n" + "no encapsulation dot1q %s\n" + "root";

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
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkNotZeroSubinterface(id);
        super.writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkNotZeroSubinterface(id);
        blockingDeleteAndRead(cli, id,
                f(DELETE_TEMPLATE,
                        Util.getSubinterfaceName(id),
                        dataBefore.getVlanId()
                                .getVlanId()
                                .getValue()));
    }

    private static void checkNotZeroSubinterface(@Nonnull InstanceIdentifier<?> id) {
        Long subifcIndex = id.firstKeyOf(Subinterface.class)
                .getIndex();
        Preconditions.checkArgument(subifcIndex != SubinterfaceReader.ZERO_SUBINTERFACE_ID,
                "Vlan configuration is not allowed for .%s subinterface", subifcIndex);
    }
}
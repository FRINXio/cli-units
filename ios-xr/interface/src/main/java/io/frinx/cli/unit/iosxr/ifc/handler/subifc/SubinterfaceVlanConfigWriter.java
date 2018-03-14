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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.subinterfaces.top.subinterfaces.Subinterface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.logical.top.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.ZERO_SUBINTERFACE_ID;
import static io.frinx.cli.unit.iosxr.ifc.handler.subifc.SubinterfaceReader.getSubinterfaceName;

public class SubinterfaceVlanConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public SubinterfaceVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private static final String MOD_TEMPLATE =
            "interface {$subIntName}\n" +
            "{% if ($delete) %}no{%endif%}encapsulation dot1Q {$vlanId.vlan_id.vlan_id.value}\n" +
            "exit";

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkNotZeroSubinterface(id);

        blockingWriteAndRead(cli, id, dataAfter,
                fT(MOD_TEMPLATE,
                        "subIntName", getSubinterfaceName(id),
                        "vlanId", dataAfter));

    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.getVlanId() == null) {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        checkNotZeroSubinterface(id);

        blockingDeleteAndRead(cli, id,
                fT(MOD_TEMPLATE,
                        "delete", true,
                        "subIntName", getSubinterfaceName(id),
                        "vlanId", dataBefore));
    }

    private static void checkNotZeroSubinterface(@Nonnull InstanceIdentifier<?> id) throws WriteFailedException {
        Long subifcIndex = id.firstKeyOf(Subinterface.class).getIndex();
        Preconditions.checkArgument(subifcIndex != ZERO_SUBINTERFACE_ID,
                "Vlan configuration is not allowed for .%s subinterface", subifcIndex);

    }
}

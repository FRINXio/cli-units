/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.cft.extension.rev200220.L2CftIfExt;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIInterfaceConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String TEMPLATE = "{% if ($delete) %}l2-cft disable port {$eth_ifc_name}\n"
            + "l2-cft unset port {$eth_ifc_name} profile\n"
            + "{% else %}l2-cft set port {$eth_ifc_name} profile {$profile}\n"
            + "{% if ($enablePresent) %}l2-cft enable port {$eth_ifc_name}\n{% endif %}{% endif %}"
            + "configuration save";

    private Cli cli;

    public L2VSIInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    private String getTemplate(String portId, String profile, Boolean delete, Boolean enablePresent) {
        return fT(TEMPLATE, "eth_ifc_name", portId, "profile", profile, "delete", delete ? Chunk.TRUE : null,
                "enablePresent", enablePresent ? Chunk.TRUE : null);
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        String portId = iid.firstKeyOf(Interface.class).getId();
        L2CftIfExt l2CftIfExtBuilder = data.getAugmentation(L2CftIfExt.class);
        String profile = l2CftIfExtBuilder.getInterfaceCft().getProfile();
        boolean profileIsNull = profile == null;
        boolean enableIsNull = l2CftIfExtBuilder.getInterfaceCft().isEnabled() == null;

        Preconditions.checkArgument(!profileIsNull, "Cannot apply l2-cft config to Interface without profile");

        if (!enableIsNull) {
            Preconditions.checkArgument(
                    Objects.equal(!l2CftIfExtBuilder.getInterfaceCft().isEnabled(), profile.isEmpty()),
                    "Cannot apply l2-cft config to Interface with false value of enabling l2-cft"
                            + " and empty profile");
            Preconditions.checkArgument(l2CftIfExtBuilder.getInterfaceCft().isEnabled(),
                    "Cannot apply l2-cft config to Interface with false value of enabling l2-cft");
            blockingWriteAndRead(cli, iid, data, getTemplate(portId, profile, false, true));
            return true;
        }

        blockingWriteAndRead(cli, iid, data, getTemplate(portId, profile, false, false));
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.getAugmentation(L2CftIfExt.class).getInterfaceCft() == null) {
            deleteCurrentAttributesWResult(iid, dataAfter, writeContext);
            return true;
        }

        writeCurrentAttributesWResult(iid, dataAfter, writeContext);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        String portId = iid.firstKeyOf(Interface.class).getId();
        blockingDeleteAndRead(cli, iid, getTemplate(portId, null, true, false));
        return true;
    }
}

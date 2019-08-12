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

package io.frinx.cli.unit.brocade.network.instance.vrf.vlan;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class DefaultVlanConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    @VisibleForTesting
    static final String WRITE_TEMPLATE = "configure terminal\n"
            + "vlan {$data.vlan_id.value}"
            + "{% if ($data.name) %} name {$data.name}\n{% else %}\n{% endif %}"
            + "end\n";

    private static final String DELETE_TEMPLATE = "configure terminal\n"
            + "no vlan {$data.vlan_id.value}\n"
            + "exit";

    private final Cli cli;

    public DefaultVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                 @Nonnull Config config,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!getCheck().canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }

        VlanConfig.Status vlanStatus = config.getStatus();
        Preconditions.checkArgument(vlanStatus == null || vlanStatus.equals(VlanConfig.Status.ACTIVE),
                "Suspended VLANs are not available");
        blockingWriteAndRead(fT(WRITE_TEMPLATE, "data", config), cli, instanceIdentifier, config);

        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        // Only "name" parameter can be changed for a VLAN, which is safe to update via write template
        return writeCurrentAttributesWResult(id, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                  @Nonnull Config config,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!getCheck().canProcess(instanceIdentifier, writeContext, true)) {
            return false;
        }

        // FIXME, consistency check: no interfaces should be using this vlan before we delete it !!

        blockingDeleteAndRead(fT(DELETE_TEMPLATE, "data", config), cli, instanceIdentifier);
        return true;
    }

    public Check getCheck() {
        return BasicCheck.checkData(
                ChecksMap.DataCheck.NetworkInstanceConfig.IID_TRANSFORMATION,
                ChecksMap.DataCheck.NetworkInstanceConfig.TYPE_DEFAULTINSTANCE);
    }
}

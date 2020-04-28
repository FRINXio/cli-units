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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.InstanceConnectionPointConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String L2VSICP_DELETE = "virtual-circuit ethernet delete vc %s";
    private static final String L2VSICP_CREATE = "virtual-circuit ethernet create vc {$name} vlan {$vlan.value}";


    private final Cli cli;

    public L2vsicpConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, false)) {
            return false;
        }

        Optional<NetworkInstance> niOpt = writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class));
        if (niOpt.isPresent()) {
            String command = createCommand(niOpt.get(), data.getName());
            blockingWriteAndRead(command, cli, iid, data);
            return true;
        }
        return false;
    }

    @VisibleForTesting
    String createCommand(@Nonnull NetworkInstance networkInstance, String name) {
        Preconditions.checkArgument(Objects.equals(networkInstance.getName(), name),
                "Network instance name and connection point id must be the same");

        String cpId = getCpId(networkInstance);
        Preconditions.checkNotNull(cpId, "Missing connection point id to create virtual circuit");

        String cpKey = getCpKey(networkInstance);
        Preconditions.checkNotNull(cpKey, "Missing connection point id to create virtual circuit");
        Preconditions.checkArgument(Objects.equals(name, cpId) && Objects.equals(name, cpKey),
                "Network instance name and connection point id must be the same");

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config
                config = getVlanId(networkInstance);
        Preconditions.checkNotNull(config, "Missing vlan config to create virtual circuit");
        return fT(L2VSICP_CREATE, "name", name, "vlan", config.getVlanId());
    }

    private String getCpId(NetworkInstance networkInstance) {
        return java.util.Optional.of(networkInstance)
                .map(NetworkInstance::getConnectionPoints)
                .map(ConnectionPoints::getConnectionPoint)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ConnectionPoint::getConnectionPointId)
                .findFirst()
                .orElse(null);
    }

    private String getCpKey(NetworkInstance networkInstance) {
        return java.util.Optional.of(networkInstance)
                .map(NetworkInstance::getConnectionPoints)
                .map(ConnectionPoints::getConnectionPoint)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ConnectionPoint::getConfig)
                .map(InstanceConnectionPointConfig::getConnectionPointId)
                .findFirst()
                .orElse(null);
    }

    private org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config
        getVlanId(NetworkInstance networkInstance) {
        return java.util.Optional.of(networkInstance)
                .map(NetworkInstance::getVlans)
                .map(Vlans::getVlan)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(Vlan::getConfig)
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        return writeCurrentAttributesWResult(iid, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, true)) {
            return false;
        }
        String name = iid.firstKeyOf(NetworkInstance.class).getName();
        blockingDeleteAndRead(f(L2VSICP_DELETE, name), cli, iid);
        return true;
    }
}

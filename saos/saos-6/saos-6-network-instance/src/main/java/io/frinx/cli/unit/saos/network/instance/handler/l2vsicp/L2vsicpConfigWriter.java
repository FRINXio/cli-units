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
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.NiVcSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.virtual.circuit.saos.extension.rev201204.SaosExtensionConfigStat;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.Vlans;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2vsicpConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String L2VSICP_DELETE = "virtual-circuit ethernet delete vc %s";
    private static final String L2VSICP_CREATE = """
            virtual-circuit ethernet create vc {$name} vlan {$vlan.value}
            {% if $set_statistics == TRUE %}virtual-circuit ethernet set vc {$name} statistics {$statistics}
            {% endif %}""";


    private final Cli cli;

    public L2vsicpConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config data,
                                                 @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, false)) {
            return false;
        }

        Optional<NetworkInstance> niOpt = writeContext.readAfter(
                Objects.requireNonNull(iid.firstIdentifierOf(NetworkInstance.class)));
        if (niOpt.isPresent()) {
            String command = createCommand(niOpt.get(), data);
            blockingWriteAndRead(command, cli, iid, data);
            return true;
        }
        return false;
    }

    @VisibleForTesting
    String createCommand(@NotNull NetworkInstance networkInstance, @NotNull Config data) {
        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config
                config = getVlanId(networkInstance);
        Preconditions.checkNotNull(config, "Missing vlan config to create virtual circuit");
        boolean onOff = getOnOff(data);
        String setStatistics = data == null ? "NULL" : Chunk.TRUE;
        String statistics = onOff ? "on" : "off";
        return fT(L2VSICP_CREATE, "name", data.getName(), "vlan", config.getVlanId(),
                "set_statistics", setStatistics, "statistics", statistics);
    }

    public static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config
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

    private boolean getOnOff(Config data) {
        return data != null && java.util.Optional.of(data)
                .map(d -> d.getAugmentation(NiVcSaosAug.class))
                .map(SaosExtensionConfigStat::isStatistics)
                .orElse(false);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config dataBefore,
                                                  @NotNull Config dataAfter,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        return writeCurrentAttributesWResult(iid, dataAfter, writeContext);
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@NotNull InstanceIdentifier<Config> iid, @NotNull Config dataBefore,
                                                  @NotNull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, true)) {
            return false;
        }
        String name = Objects.requireNonNull(iid.firstKeyOf(NetworkInstance.class)).getName();
        blockingDeleteAndRead(f(L2VSICP_DELETE, name), cli, iid);
        return true;
    }
}
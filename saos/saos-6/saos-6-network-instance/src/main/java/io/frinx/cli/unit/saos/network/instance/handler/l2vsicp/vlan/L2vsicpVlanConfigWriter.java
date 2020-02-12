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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.vlan;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsicp.L2vsicpReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config2;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.SaosVlanExtensionConfigStat;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class L2vsicpVlanConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    private static final String WRITE_VLAN_COMMAND =
            "{$data|update(value,virtual-circuit ethernet set vc `$name` vlan `$data.value`, )}";
    private static final String WRITE_STAT_COMMAND = "{% if $set_statistics == TRUE %}"
            + "virtual-circuit ethernet set vc {$name} statistics {$statistics}"
            + "{% endif %}";

    private final Cli cli;

    public L2vsicpVlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, false)) {
            return false;
        }

        String name = iid.firstKeyOf(NetworkInstance.class).getName();
        String command = createCommand(null, data, name, false);
        Optional<NetworkInstance> niOpt = writeContext.readBefore(iid.firstIdentifierOf(NetworkInstance.class));
        if (!niOpt.isPresent()) {
            command = createCommand(null, data, name, true);
        }
        blockingWriteAndRead(command, cli, iid, data);
        return true;
    }

    @VisibleForTesting
    String createCommand(Config dataBefore, @Nonnull Config data, String name, boolean onlyStat) {
        String command = fT(WRITE_VLAN_COMMAND, "name", name, "before",
                dataBefore == null ? null : dataBefore.getVlanId(), "data", data.getVlanId());
        boolean onOffBefore = getOnOff(dataBefore);
        boolean onOff = getOnOff(data);
        String setStatistics = dataBefore == null ? "TRUE" : onOff == onOffBefore ? "NULL" : "TRUE";
        String statistics = onOff ? "on" : "off";
        String statCommand = fT(WRITE_STAT_COMMAND, "name", name,
                "set_statistics", setStatistics, "statistics", statistics);
        return onlyStat ? statCommand :
                command.isEmpty() ? statCommand :
                        statCommand.isEmpty() ? command : command + "\n" + statCommand;
    }

    private boolean getOnOff(Config data) {
        return data == null ? false : java.util.Optional.of(data)
                .map(d -> d.getAugmentation(Config2.class))
                .filter(Objects::nonNull)
                .map(SaosVlanExtensionConfigStat::isStatistics)
                .orElse(false);
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, false)) {
            return false;
        }

        String name = iid.firstKeyOf(NetworkInstance.class).getName();
        blockingWriteAndRead(createCommand(dataBefore, dataAfter, name, true), cli, iid, dataAfter);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid, @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!L2vsicpReader.L2VSICP_CHECK.canProcess(iid, writeContext, true)) {
            return false;
        }
        String name = iid.firstKeyOf(NetworkInstance.class).getName();
        Optional<NetworkInstance> vlanOpt = writeContext.readAfter(iid.firstIdentifierOf(NetworkInstance.class));
        if (vlanOpt.isPresent()) {
            Preconditions.checkState(!(vlanOpt.get().getVlans() == null
                    || vlanOpt.get().getVlans().getVlan() == null
                    || vlanOpt.get().getVlans().getVlan().isEmpty()),
                    f("Cannot delete vlan from network instance '%s'", name));
        }
        return true;
    }
}

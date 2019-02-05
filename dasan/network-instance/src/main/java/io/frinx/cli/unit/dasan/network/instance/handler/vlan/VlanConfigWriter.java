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

package io.frinx.cli.unit.dasan.network.instance.handler.vlan;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.dasan.rev180801.Config1;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanConfigWriter implements CliWriter<Config> {
    private final Cli cli;

    public VlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
            @Nonnull InstanceIdentifier<Config> instanceIdentifier,
            @Nonnull Config config,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        Preconditions.checkArgument(
            NetworInstance.DEFAULT_NETWORK.equals(instanceIdentifier.firstKeyOf(NetworkInstance.class)),
            "vlan must be configured in default network instance");

        writeOrUpdateVlanCreate(instanceIdentifier, config, false);
    }


    @Override
    public void updateCurrentAttributes(
            @Nonnull InstanceIdentifier<Config> instanceIdentifier,
            @Nonnull Config dataBefore,
            @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        writeOrUpdateVlanCreate(instanceIdentifier, dataAfter, true);
    }

    @Override
    public void deleteCurrentAttributes(
            @Nonnull InstanceIdentifier<Config> instanceIdentifier,
            @Nonnull Config config,
            @Nonnull WriteContext writeContext) throws WriteFailedException.DeleteFailedException {

        blockingDeleteAndRead(cli, instanceIdentifier,
                "configure terminal",
                "bridge",
                f("no vlan %d", config.getVlanId().getValue()),
                "end");
    }

    private void writeOrUpdateVlanCreate(
            InstanceIdentifier<Config> instanceIdentifier,
            Config config,
            boolean isPresent) throws WriteFailedException {

        Integer vlanId = config.getVlanId().getValue();
        String elineOpt = hasElineOption(config) ? "eline" : "";

        // Dasan does not allow to update vlan config,
        // so we need to delete the existing data and put new data.
        blockingWriteAndRead(cli, instanceIdentifier, config,
                "configure terminal",
                "bridge",
                isPresent ? f("no vlan %d", vlanId) : "",
                f("vlan create %d %s", vlanId, elineOpt),
                "end");
    }

    private static boolean hasElineOption(Config config) {
        if (config == null) {
            return false;
        }

        Config1 augmentConfig = config.getAugmentation(Config1.class);
        if (augmentConfig == null) {
            return false;
        }

        return Boolean.TRUE.equals(augmentConfig.isEline());
    }
}

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

package io.frinx.cli.unit.saos.network.instance.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanConfigWriter implements CliWriter<Config> {

    private static final String VLAN_WITH_NAME = "vlan create vlan %d name %s";
    private static final String VLAN_DEFAULT = "vlan create vlan %d";

    private final Cli cli;

    public VlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {

        blockingWriteAndRead(writeTemplate(data), cli, instanceIdentifier, data);
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        if (config.getName() == null) {
            return f(VLAN_DEFAULT, config.getVlanId().getValue());
        } else {
            return f(VLAN_WITH_NAME, config.getVlanId().getValue(), config.getName());
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.getName() != null) {
            if (!(dataAfter.getName().equals(dataBefore.getName()))) {
                blockingWriteAndRead(
                        f("vlan rename vlan %d name %s", dataAfter.getVlanId().getValue(), dataAfter.getName()),
                        cli, id, dataAfter);
            }
        } else {
            blockingWriteAndRead(f("vlan rename vlan %d", dataAfter.getVlanId().getValue()),
                    cli, id, dataAfter);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(f("vlan delete vlan %d", config.getVlanId().getValue()), cli, instanceIdentifier);
    }
}

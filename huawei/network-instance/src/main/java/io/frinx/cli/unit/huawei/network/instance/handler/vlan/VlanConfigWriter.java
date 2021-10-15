/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.huawei.network.instance.handler.vlan;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.vlan.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.saos.rev200210.Config1;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VlanConfigWriter implements CliWriter<Config> {

    private static final String WRITE_UPDATE_TEMPL = "system-view\n"
            + "vlan {$data.vlan_id.value}\n"
            + "{% if ($augData.description) %}description {$augData.description}\n{% endif %}"
            + "return\n";

    private static final String DELETE_TEMPL = "system-view\n"
            + "undo vlan {$data.vlan_id.value}\n"
            + "return\n";

    private final Cli cli;

    public VlanConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull Config config,
                                      @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, instanceIdentifier, config, writeTemplate(config));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                        @Nonnull Config config,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(deleteTemplate(config), cli, instanceIdentifier);
    }

    @VisibleForTesting
    String writeTemplate(Config config) {
        Config1 augData = config.getAugmentation(Config1.class);
        return fT(WRITE_UPDATE_TEMPL,
                "data", config, "augData", augData);
    }

    @VisibleForTesting
    String deleteTemplate(Config config) {
        return fT(DELETE_TEMPL, "data", config);
    }

}

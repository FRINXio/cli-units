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

package io.frinx.cli.unit.iosxr.evpn.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.evpn.rev181112.evpn.top.evpn.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class EvpnConfigWriter implements CliWriter<Config> {

    private Cli cli;
    // templates
    static final String T_WRITE_EVPN = "evpn\n"
            + "{% if $cost_out != TRUE %}no {% endif %}cost-out\n"
            + "{% if $create == TRUE %}"
            + "{% if $startup_cost_in %}startup-cost-in {$startup_cost_in}\n"
            + "{% endif %}"
            + "{% else %}"
            + "{% if $startup_cost_in %}startup-cost-in {$startup_cost_in}\n"
            + "{% elseIf $old_startup_cost_in %}"
            + "no startup-cost-in {$old_startup_cost_in}\n"
            + "{% endif %}"
            + "{% endif %}"
            + "root\n";

    static final String CMD_DELETE_EVPN = "no evpn\n";

    public EvpnConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        String cmd = fT(T_WRITE_EVPN,
                "create", true,
                "cost_out", BooleanUtils.isTrue(data.isCostOut()),
                "startup_cost_in", data.getStartupCostIn());
        blockingWriteAndRead(cli, id, data, cmd);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        String cmd = fT(T_WRITE_EVPN,
                "create", false,
                "cost_out", BooleanUtils.isTrue(dataAfter.isCostOut()),
                "startup_cost_in", dataAfter.getStartupCostIn(),
                "old_startup_cost_in", dataBefore.getStartupCostIn());
        blockingWriteAndRead(cli, id, dataAfter, cmd);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingDeleteAndRead(cli, id,CMD_DELETE_EVPN);
    }
}

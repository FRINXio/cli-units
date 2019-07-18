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

package io.frinx.cli.unit.iosxr.oam.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.apache.commons.lang3.BooleanUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = "ethernet cfm\n"
        + "root";

    private static final String DELETE_TEMPLATE = "no ethernet cfm";

    public CfmConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        if (BooleanUtils.isTrue(config.isEnabled())) {
            blockingWriteAndRead(cli, id, config, CREATE_TEMPLATE);
        }
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        if (BooleanUtils.isTrue(dataAfter.isEnabled())) {
            writeCurrentAttributes(id, dataAfter, writeContext);
        } else {
            deleteCurrentAttributes(id, dataBefore, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        blockingDeleteAndRead(cli, id, DELETE_TEMPLATE);
    }
}

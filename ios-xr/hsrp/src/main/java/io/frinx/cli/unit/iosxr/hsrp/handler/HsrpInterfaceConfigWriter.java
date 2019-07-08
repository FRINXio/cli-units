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

package io.frinx.cli.unit.iosxr.hsrp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.hsrp.rev180814._interface.top.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class HsrpInterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public HsrpInterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        writeInterface(id, data);
    }

    private void writeInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.CreateFailedException {

        blockingWriteAndRead(cli, id, data,
                "router hsrp",
                f("interface %s", data.getInterfaceId()),
                data.getMinimumDelay() == null || data.getReloadDelay() == null
                        ? "" : f("hsrp delay minimum %s reload %s", data.getMinimumDelay(), data.getReloadDelay()),
                "root");
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        blockingWriteAndRead(cli, id, dataAfter,
                "router hsrp",
                f("interface %s", dataAfter.getInterfaceId()),
                dataAfter.getMinimumDelay() == null || dataAfter.getReloadDelay() == null
                        ? dataBefore.getMinimumDelay() != null && dataBefore.getReloadDelay() != null
                                ? "no hsrp delay" : ""
                        : f("hsrp delay minimum %s reload %s", dataAfter.getMinimumDelay(), dataAfter.getReloadDelay()),
                "root");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
            @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteInterface(id, dataBefore);
    }

    private void deleteInterface(InstanceIdentifier<Config> id, Config data)
            throws WriteFailedException.DeleteFailedException {
        blockingDeleteAndRead(cli, id,
                "router hsrp",
                f("no interface %s", data.getInterfaceId()),
                "root");
    }
}

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

package io.frinx.cli.unit.huawei.system.handler.factory;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.honeycomb.translate.write.WriteFailedException.DeleteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.system.huawei.factory.config.extension.rev211108.huawei.factory.configuration.factory.configuration.status.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class FactoryConfigurationConfigWriter implements CliWriter<Config> {

    private static final String FACTORY_CONFIG_PROHIBIT = "system-view\n"
            + "{% if($prohibit == TRUE) %}"
            + "factory-config prohibit\n"
            + "{% else %}"
            + "undo factory-config prohibit\n"
            + "{% endif %}"
            + "return";

    private final Cli cli;

    public FactoryConfigurationConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        boolean prohibit = config.isFactoryConfigurationProhibited();
        blockingWriteAndRead(cli,id, config, fT(FACTORY_CONFIG_PROHIBIT, "prohibit", prohibit));
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        boolean prohibit = dataAfter.isFactoryConfigurationProhibited();
        blockingWriteAndRead(cli, id, dataAfter, fT(FACTORY_CONFIG_PROHIBIT, "prohibit", prohibit));
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config config,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        throw new DeleteFailedException(id,
                new IllegalStateException("Deleting Factory config status is not permitted."));
    }

}

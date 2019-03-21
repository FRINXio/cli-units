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

package io.frinx.cli.ni.base.handler.vrf;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeChildWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class AbstractL3VrfConfigWriter implements CliWriter<Config>, CompositeChildWriter<Config> {

    private final Cli cli;

    public AbstractL3VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if (config.getType().equals(L3VRF.class)) {
            blockingWriteAndRead(cli, instanceIdentifier, config, updateTemplate(null, config));
            return true;
        }
        return false;
    }

    protected abstract String updateTemplate(Config before, Config after);

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore, @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext)
            throws WriteFailedException.UpdateFailedException {

        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                "Changing interface type is not permitted. Before: %s, After: %s",
                dataBefore.getType(), dataAfter.getType());

        if (dataAfter.getType().equals(L3VRF.class)) {
            try {
                blockingWriteAndRead(cli, id, dataAfter, updateTemplate(dataBefore, dataAfter));
            } catch (WriteFailedException.CreateFailedException e) {
                throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                                  @Nonnull Config config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        if (config.getType().equals(L3VRF.class)) {
            blockingDeleteAndRead(cli, instanceIdentifier, deleteTemplate(config));
            return true;
        }
        return false;
    }

    protected abstract String deleteTemplate(Config config);
}

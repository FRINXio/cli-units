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

package io.frinx.cli.handlers.def;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.DEFAULTINSTANCE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class DefaultConfigWriter implements CliWriter<Config> {

    private static final IllegalArgumentException EX = new IllegalArgumentException("Default network instance cannot "
            + "be manipulated");

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        if (config.getType()
                .equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.CreateFailedException(instanceIdentifier, config, EX);
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore, @Nonnull
            Config dataAfter, @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        if (dataAfter.getType()
                .equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.UpdateFailedException(id, dataBefore, dataAfter, EX);
        }
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull Config
            config, @Nonnull WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {

        if (config.getType()
                .equals(DEFAULTINSTANCE.class)) {
            throw new WriteFailedException.DeleteFailedException(instanceIdentifier, EX);
        }
    }
}

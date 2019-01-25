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

package io.frinx.cli.unit.junos.network.instance.handler.vrf;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeChildWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L3VrfConfigWriter implements CliWriter<Config>, CompositeChildWriter<Config> {

    private Cli cli;

    public L3VrfConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull Config data,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!isSupportedType(data)) {
            return false;
        }

        createOrUpdateCurrentAttributes(id, data);
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(
            @Nonnull InstanceIdentifier<Config> id,
            @Nonnull Config dataBefore,
            @Nonnull Config dataAfter,
            @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!isSupportedType(dataAfter)) {
            return false;
        }

        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
            "Changing interface type is not permitted. Before: %s, After: %s",
            dataBefore.getType(), dataAfter.getType());

        createOrUpdateCurrentAttributes(id, dataAfter);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {

        if (!isSupportedType(dataBefore)) {
            return false;
        }

        String name = id.firstKeyOf(NetworkInstance.class).getName();

        blockingDeleteAndRead(cli, id, f("delete routing-instances %s", name));
        return true;
    }

    @VisibleForTesting
    String createOrUpdateCurrentAttributes(
        @Nonnull InstanceIdentifier<Config> id,
        @Nonnull Config data) throws WriteFailedException {

        String name = id.firstKeyOf(NetworkInstance.class).getName();
        return blockingWriteAndRead(cli, id, data, f("set routing-instances %s instance-type virtual-router", name));
    }

    private boolean isSupportedType(Config data) {
        return data.getType() == L3VRF.class;
    }
}

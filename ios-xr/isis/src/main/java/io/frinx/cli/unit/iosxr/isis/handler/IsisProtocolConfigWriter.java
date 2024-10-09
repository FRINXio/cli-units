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

package io.frinx.cli.unit.iosxr.isis.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisProtocolConfigWriter implements CliWriter<Config>, CompositeWriter.Child<Config> {

    private Cli cli;

    public IsisProtocolConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(
        @NotNull InstanceIdentifier<Config> iid,
        @NotNull Config data,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!ChecksMap.PathCheck.Protocol.ISIS.canProcess(iid, writeContext, false)) {
            return false;
        }

        String vrfName = iid.firstKeyOf(NetworkInstance.class).getName();
        Preconditions.checkState(NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName),
            f("IS-IS configuration should be set in default network: %s", vrfName));

        String instanceName = iid.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, iid, data,
            f("router isis %s", instanceName),
            "root");
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(
        @NotNull InstanceIdentifier<Config> iid,
        @NotNull Config dataBefore,
        @NotNull Config dataAfter,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!ChecksMap.PathCheck.Protocol.ISIS.canProcess(iid, writeContext, false)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(
        @NotNull InstanceIdentifier<Config> iid,
        @NotNull Config dataBefore,
        @NotNull WriteContext writeContext) throws WriteFailedException {

        if (!ChecksMap.PathCheck.Protocol.ISIS.canProcess(iid, writeContext, true)) {
            return false;
        }

        final String instanceName = iid.firstKeyOf(Protocol.class).getName();

        blockingWriteAndRead(cli, iid, dataBefore,
            f("no router isis %s", instanceName));
        return true;
    }
}
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

package io.frinx.cli.ios.bgp.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolWriter implements CompositeWriter.Child<Config> {

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.BGP.canProcess(iid, writeContext, false)) {
            return false;
        }

        // noop
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.BGP.canProcess(iid, writeContext, false)) {
            return false;
        }

        // noop
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> iid,
                                                  @Nonnull Config config,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.BGP.canProcess(iid, writeContext, true)) {
            return false;
        }

        // noop
        return true;
    }
}

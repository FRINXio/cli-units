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

package io.frinx.cli.iosxr.mpls.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.mpls.MplsWriter;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoadShareConfigWriter implements MplsWriter<Config> {

    private Cli cli;

    public LoadShareConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class)
                .getName();
        if (data.getLoadShare() == null) {
            return;
        }
        blockingWriteAndRead(cli, id, data,
                f("interface tunnel-te %s", name),
                f("load-share %s", data.getLoadShare()),
                "root");
    }

    @Override
    public void updateCurrentAttributesForType(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.getLoadShare() == null) {
            this.deleteCurrentAttributesForType(id, dataBefore, writeContext);
        } else {
            this.writeCurrentAttributesForType(id, dataAfter, writeContext);
        }
    }


    @Override
    public void deleteCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class)
                .getName();
        blockingWriteAndRead(cli, id, data,
                f("interface tunnel-te %s", name),
                f("no load-share %s", data.getLoadShare()),
                "root");
    }
}
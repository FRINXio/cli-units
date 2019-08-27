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

package io.frinx.cli.unit.ios.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.cisco.rev171024.cisco.mpls.te.tunnel.top.cisco.mpls.te.extension.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class LoadShareConfigWriter implements CliWriter<Config> {

    private static final String WRITE_TEMPLATE = "configure terminal\n"
            + "interface tunnel {$name}\n"
            + "tunnel mode mpls traffic-eng\n"
            + "{% if ($delete) %}no {% endif %}tunnel mpls traffic-eng load-share {$load-share}\n"
            + "end\n";

    private Cli cli;

    public LoadShareConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull
            WriteContext writeContext) throws WriteFailedException {
        final String tunnelInterfaceName = id.firstKeyOf(Tunnel.class)
                .getName();
        if (data.getLoadShare() == null) {
            return;
        }
        blockingWriteAndRead(getCommand(tunnelInterfaceName,data,false),cli, id, data);

    }

    @VisibleForTesting
    private String getCommand(String tunnelInterfaceName,Config data, boolean delete) {
        return fT(WRITE_TEMPLATE,"name",tunnelInterfaceName,"load-share", data.getLoadShare(),
                "delete", delete ? Chunk.TRUE : null);

    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        if (dataAfter.getLoadShare() == null) {
            this.deleteCurrentAttributes(id, dataBefore, writeContext);
        } else {
            this.writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull
            WriteContext writeContext) throws WriteFailedException {
        final String tunnelInterfaceName = id.firstKeyOf(Tunnel.class)
                .getName();
        blockingDeleteAndRead(getCommand(tunnelInterfaceName,data, true),cli, id);
    }
}
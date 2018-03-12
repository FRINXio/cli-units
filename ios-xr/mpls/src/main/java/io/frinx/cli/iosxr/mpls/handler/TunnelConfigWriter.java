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

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;

public class TunnelConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public TunnelConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class).getName();

        checkTunnelConfig(data);

        blockingWriteAndRead(cli, id, data,
            f("interface tunnel-te %s", name),
            (data.isShortcutEligible()) ? "autoroute announce" : "no autoroute announce",
            (data.getMetric() != null && LSPMETRICABSOLUTE.class.equals(data.getMetricType())) ? f("metric absolute %s", data.getMetric()) : "no metric absolute",
            "exit");
    }

    private static void checkTunnelConfig(Config data) {

        // TODO What if metric-type is set but metric is not
        if (data.getMetric() != null) {
            Preconditions.checkArgument(LSPMETRICABSOLUTE.class.equals(data.getMetricType()),
                    "Only LSP_METRIC_ABSOLUTE metric type is supported");

            Preconditions.checkArgument(data.isShortcutEligible(),
                    "Cannot configure metric on non shortcut-eligible tunnel");
        }
    }

    @Override
    public void updateCurrentAttributes(@Nonnull final InstanceIdentifier<Config> id,
                                        @Nonnull final Config dataBefore,
                                        @Nonnull final Config dataAfter,
                                        @Nonnull final WriteContext writeContext) throws WriteFailedException {
        this.writeCurrentAttributes(id, dataAfter, writeContext);
    }


    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config data, @Nonnull WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class).getName();
        blockingWriteAndRead(cli, id, data,
            f("no interface tunnel-te %s", name));
    }
}

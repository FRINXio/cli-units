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

package io.frinx.cli.unit.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.tunnel.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.types.rev170824.LSPMETRICABSOLUTE;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TunnelConfigWriter implements CliWriter<Config> {

    private static final String INPUT_T = """
            {% if ($delete) %}no {% endif %}interface tunnel-te {$name}
            {% if (!$delete) %}{% if (!$autoroute) %}no {% endif %}autoroute announce
            {% if ($autoroute) %}{% if ($metric == nondefined) %}no metric absolute
            {% else if ( $metric != $null) %}metric absolute {$metric}
            {% endif %}{% endif %}root{% endif %}""";

    private Cli cli;

    public TunnelConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class)
                .getName();

        checkTunnelConfig(data);

        blockingWriteAndRead(cli, id, data,
                fT(INPUT_T, "name", name, "autoroute", data.isShortcutEligible() ? true : null,
                        "metric", LSPMETRICABSOLUTE.class.equals(data.getMetricType()) ? data.getMetric() : null));
    }

    @VisibleForTesting
    static void checkTunnelConfig(Config data) {
        if (data.isShortcutEligible() == null || !data.isShortcutEligible()) {
            Preconditions.checkArgument(data.getMetric() == null, "metric cannot be defined in MPLS "
                    + "tunnel " + data.getName() + " because 'shortcut-eligible' is not defined or is 'false'");
            Preconditions.checkArgument(data.getMetricType() == null, "metric-type cannot be defined in MPLS "
                    + "tunnel " + data.getName() + " because 'shortcut-eligible' is not defined or is 'false'");
        } else {
            if (data.getMetricType() == null || data.getMetric() == null) {
                Preconditions.checkArgument(data.getMetric() == null, "metric is defined but metric-type is not in "
                        + "MPLS tunnel " + data.getName());
                Preconditions.checkArgument(data.getMetricType() == null, "metric-type is defined but metric is not "
                        + "in MPLS tunnel " + data.getName());
            } else {
                Preconditions.checkArgument(LSPMETRICABSOLUTE.class.equals(data.getMetricType()),
                        "Only LSP_METRIC_ABSOLUTE metric type is supported");
            }
        }
    }

    @Override
    public void updateCurrentAttributes(@NotNull final InstanceIdentifier<Config> id,
                                        @NotNull final Config dataBefore,
                                        @NotNull final Config dataAfter,
                                        @NotNull final WriteContext writeContext) throws WriteFailedException {
        if (wasMetricDefined(dataBefore, dataAfter)) {
            final String name = id.firstKeyOf(Tunnel.class)
                    .getName();

            checkTunnelConfig(dataAfter);

            blockingWriteAndRead(cli, id, dataAfter,
                    fT(INPUT_T, "name", name, "autoroute", dataAfter.isShortcutEligible() ? true : null,
                            "metric", "nondefined"));
        } else {
            this.writeCurrentAttributes(id, dataAfter, writeContext);
        }
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config data, @NotNull
            WriteContext writeContext) throws WriteFailedException {
        final String name = id.firstKeyOf(Tunnel.class)
                .getName();
        blockingWriteAndRead(cli, id, data,
                fT(INPUT_T, "name", name, "delete", true));
    }

    private boolean wasMetricDefined(@NotNull final Config dataBefore, @NotNull final Config dataAfter) {
        return dataBefore.getMetric() != null && dataBefore.getMetricType() != null
                && dataAfter.getMetric() == null && dataAfter.getMetricType() == null;
    }
}
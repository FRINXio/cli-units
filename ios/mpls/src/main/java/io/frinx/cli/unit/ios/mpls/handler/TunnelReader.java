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
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TunnelReader implements CliConfigListReader<Tunnel, TunnelKey, TunnelBuilder> {

    private Cli cli;

    private static final String SH_RUN_INTERFACE = "show running-config | include ^interface";
    private static final Pattern TUNNEL_IFACE_LINE = Pattern.compile("interface Tunnel(?<name>[0-9]+)");

    public TunnelReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public List<TunnelKey> getAllIds(@NotNull InstanceIdentifier<Tunnel> instanceIdentifier, @NotNull
            ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_RUN_INTERFACE, cli, instanceIdentifier, readContext);
        return getTunnelKeys(output);
    }

    @VisibleForTesting
    static List<TunnelKey> getTunnelKeys(String output) {
        return ParsingUtils.parseFields(output, 0, TUNNEL_IFACE_LINE::matcher,
            matcher -> matcher.group("name"), TunnelKey::new);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Tunnel> instanceIdentifier, @NotNull
            TunnelBuilder tunnelBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        TunnelKey key = instanceIdentifier.firstKeyOf(Tunnel.class);
        tunnelBuilder.setName(key.getName());
    }
}
/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.mpls.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.mpls.MplsListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.TunnelsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.Tunnel;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.mpls.rev170824.te.tunnels_top.tunnels.TunnelKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class TunnelReader implements MplsListReader.MplsConfigListReader<Tunnel, TunnelKey, TunnelBuilder> {

    private Cli cli;

    private static final String SH_RSVP_INT = "show interfaces tunnel-te * | include line";
    private static final Pattern IFACE_LINE = Pattern.compile("tunnel-te(?<name>[0-9]+) is (?<status>[^,]+), line protocol is (?<lineStatus>.+)");

    public TunnelReader(Cli cli) {
        this.cli = cli;
    }


    @Override
    public List<TunnelKey> getAllIdsForType(@Nonnull InstanceIdentifier<Tunnel> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        String output = blockingRead(SH_RSVP_INT, cli, instanceIdentifier, readContext);
        return getTunnelKeys(output);
    }

    @VisibleForTesting
    public static List<TunnelKey> getTunnelKeys(String output) {
        return ParsingUtils.parseFields(output, 0, IFACE_LINE::matcher,
            matcher -> matcher.group("name"), TunnelKey::new);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Tunnel> readData) {
        ((TunnelsBuilder) builder).setTunnel(readData);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Tunnel> instanceIdentifier, @Nonnull TunnelBuilder tunnelBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        TunnelKey key = instanceIdentifier.firstKeyOf(Tunnel.class);
        tunnelBuilder.setName(key.getName());
    }

    @Nonnull
    @Override
    public TunnelBuilder getBuilder(@Nonnull InstanceIdentifier<Tunnel> id) {
        return new TunnelBuilder();
    }
}

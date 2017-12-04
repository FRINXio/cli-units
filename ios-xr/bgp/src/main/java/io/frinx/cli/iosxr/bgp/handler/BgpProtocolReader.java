/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliListReader;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        BgpReader.BgpConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private static final String SHOW_BGP_INSTANCES = "show bgp instances";
    public static final Pattern INSTANCE_LINE = Pattern.compile("(?<id>[0-9.]+) (?<vrf>[^\\s]+) (?<instance>[^\\s]+) (?<as>[0-9.]+) (?<vrfs>[0-9.]+) (?<afi>.*)");
    private final Cli cli;

    public BgpProtocolReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> iid,
                                       @Nonnull ReadContext context) throws ReadFailedException {
        String output = blockingRead(SHOW_BGP_INSTANCES, cli, iid, context);
        return ParsingUtils.parseFields(output.replaceAll("\\h+", " "), 0,
                INSTANCE_LINE::matcher,
                matcher -> matcher.group("instance"),
                v -> new ProtocolKey(TYPE, v));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> iid,
                                             @Nonnull ProtocolBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        ProtocolKey key = iid.firstKeyOf(Protocol.class);
        builder.setName(key.getName());
        builder.setIdentifier(key.getIdentifier());
    }
}

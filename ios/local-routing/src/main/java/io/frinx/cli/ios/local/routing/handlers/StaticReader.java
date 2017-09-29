/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.StaticLocalRoutingProtocolReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top.StaticRoutesBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.IpPrefix;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.types.inet.rev170403.Ipv4Prefix;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StaticReader implements CliListReader<Static, StaticKey, StaticBuilder> {

    private static final String DEFAULT_VRF = "default";
    private static final String SH_IP_ROUTE= "sh ip route static";
    private static final Pattern INTERFACE_IP_LINE =
            Pattern.compile("\\s*S        (?<ip>[^/]+/[0-9][0-9]).*");

    private Cli cli;

    public StaticReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<StaticKey> getAllIds(@Nonnull InstanceIdentifier<Static> instanceIdentifier,
                                     @Nonnull ReadContext readContext) throws ReadFailedException {

        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(StaticLocalRoutingProtocolReader.TYPE)) {
            return Collections.emptyList();
        }

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (vrfName.equals(DEFAULT_VRF)) {
            return parseStatic(blockingRead(SH_IP_ROUTE, cli, instanceIdentifier, readContext));
        }

        return new ArrayList<>();
    }

    @VisibleForTesting
    static List<StaticKey> parseStatic(String output) {
        return ParsingUtils.parseFields(output, 0,
                INTERFACE_IP_LINE::matcher,
                m -> m.group("ip"),
                value -> new StaticKey(new IpPrefix(new Ipv4Prefix(value))));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Static> list) {
        ((StaticRoutesBuilder) builder).setStatic(list);
    }

    @Nonnull
    @Override
    public StaticBuilder getBuilder(@Nonnull InstanceIdentifier<Static> instanceIdentifier) {
        return new StaticBuilder();
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Static> instanceIdentifier,
                                      @Nonnull StaticBuilder staticBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        ProtocolKey protocolKey = instanceIdentifier.firstKeyOf(Protocol.class);
        if (!protocolKey.getIdentifier().equals(StaticLocalRoutingProtocolReader.TYPE)) {
            return;
        }

        staticBuilder.setPrefix(instanceIdentifier.firstKeyOf(Static.class).getPrefix());
    }

}

/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf.handler;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        OspfReader.OspfConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private Cli cli;

    public OspfProtocolReader(Cli cli) {
        this.cli = cli;
    }

    private static final String SH_RUN_OSPF = "do show running-config router ospf | include ^router ospf";
    private static final Pattern ROUTER_OSPF_LINE = Pattern.compile("router ospf (?<ospfName>\\S+)");

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext)
            throws ReadFailedException {
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        // TODO We should add this check in all descendant readers
        Preconditions.checkArgument(DEFAULT_NETWORK_NAME.equals(vrfId), "VRF-aware OSPF is not supported");

        String output = blockingRead(SH_RUN_OSPF, cli, instanceIdentifier, readContext);

        return parseOspfIds(output);
    }

    @VisibleForTesting
    public static List<ProtocolKey> parseOspfIds(String output) {
        return ParsingUtils.parseFields(output, 0,
                ROUTER_OSPF_LINE::matcher,
                matcher -> matcher.group("ospfName"),
                s -> new ProtocolKey(TYPE, s));
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                             @Nonnull ProtocolBuilder protocolBuilder,
                                             @Nonnull ReadContext readContext) {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setName(key.getName());
        protocolBuilder.setIdentifier(key.getIdentifier());
    }

}

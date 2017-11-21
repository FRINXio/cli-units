/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ospf;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;
import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ospf.common.OspfReader;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        OspfReader.OspfConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private Cli cli;

    public OspfProtocolReader(Cli cli) {
        this.cli = cli;
    }

    private static final Pattern OSPF_NO_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+)\\s*");
    private static final Pattern OSPF_VRF = Pattern.compile("\\s*router ospf (?<id>[^\\s]+) vrf (?<vrf>[^\\s]+)\\s*");

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext)
            throws ReadFailedException {
        String output = blockingRead("sh run | include ospf", cli, instanceIdentifier, readContext);
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();

        if (vrfId.equals(DEFAULT_NETWORK_NAME)) {
            return ParsingUtils.parseFields(output, 0,
                    OSPF_NO_VRF::matcher,
                    matcher -> matcher.group("id"),
                    s -> new ProtocolKey(TYPE, s));
        } else {
            return NEWLINE.splitAsStream(output)
                    .map(String::trim)
                    // Only include those from VRF
                    .filter(s -> s.contains("vrf " + vrfId))
                    .map(OSPF_VRF::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group("id"))
                    .map(s -> new ProtocolKey(TYPE, s))
                    .collect(Collectors.toList());
        }
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

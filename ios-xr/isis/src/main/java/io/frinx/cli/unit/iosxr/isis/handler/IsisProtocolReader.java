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

package io.frinx.cli.unit.iosxr.isis.handler;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.ISIS;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IsisProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private static final String SH_RUN_ROUTER_ISIS = "show running-config router isis | include ^router isis";
    private static final Pattern ROUTER_ISIS_LINE = Pattern.compile("router isis (?<instance>\\S+)");

    private Cli cli;

    public IsisProtocolReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(
        @Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (!NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            return Collections.emptyList();
        }
        String output = blockingRead(SH_RUN_ROUTER_ISIS, cli, instanceIdentifier, readContext);

        return parseIsisIds(output);
    }

    private static List<ProtocolKey> parseIsisIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            ROUTER_ISIS_LINE::matcher,
            m -> m.group("instance"),
            s -> new ProtocolKey(ISIS.class, s));
    }

    @Override
    public void readCurrentAttributes(
        @Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
        @Nonnull ProtocolBuilder protocolBuilder,
        @Nonnull ReadContext readContext) {

        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setName(key.getName());
        protocolBuilder.setIdentifier(key.getIdentifier());
    }

    @Override
    public Check getCheck() {
        return ChecksMap.PathCheck.Protocol.ISIS;
    }
}

/*
 * Copyright © 2021 Frinx and others.
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

package io.frinx.cli.unit.iosxe.bgp.handler;

import com.google.common.collect.ImmutableList;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    public static final String DEFAULT_BGP_INSTANCE = "default";
    private final Cli cli;

    public BgpProtocolReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> iid,
                                       @Nonnull ReadContext context) throws ReadFailedException {
        String output = blockingRead("show running-config | include router bgp", cli, iid, context);
        if (output.isEmpty()) {
            return Collections.emptyList();
        }

        // IOS does not support multi-instance BGP therefore there is only default instance
        return ImmutableList.of(new ProtocolKey(BGP.class, DEFAULT_BGP_INSTANCE));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> iid,
                                      @Nonnull ProtocolBuilder builder,
                                      @Nonnull ReadContext ctx) throws ReadFailedException {
        if (!NetworInstance.DEFAULT_NETWORK.equals(iid.firstKeyOf(NetworkInstance.class))) {
            setBaseAttributes(iid, builder);
            //todo check if more attributes need to be handled for BGP VRF-awareness
            return;
        }

        setBaseAttributes(iid, builder);
    }

    private void setBaseAttributes(@Nonnull InstanceIdentifier<Protocol> iid, @Nonnull ProtocolBuilder builder) {
        ProtocolKey key = iid.firstKeyOf(Protocol.class);
        builder.setName(key.getName());
        builder.setIdentifier(key.getIdentifier());
    }

    @Override
    public Check getCheck() {
        return ChecksMap.PathCheck.Protocol.BGP;
    }
}

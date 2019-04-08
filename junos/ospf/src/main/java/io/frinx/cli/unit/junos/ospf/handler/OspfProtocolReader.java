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

package io.frinx.cli.unit.junos.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private Cli cli;

    public OspfProtocolReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static final String OSPF_NAME = "default";
    @VisibleForTesting
    static final String SH_RUN_OSPF = "show configuration protocols ospf | display set";
    @VisibleForTesting
    static final String SH_RUN_OSPF_VRF = "show configuration routing-instances | display set | match ospf";

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext)
            throws ReadFailedException {
        List<ProtocolKey> rtn = new ArrayList<>();

        String output = blockingRead(SH_RUN_OSPF, cli, instanceIdentifier, readContext);
        String outputVrf = blockingRead(SH_RUN_OSPF_VRF, cli, instanceIdentifier, readContext);
        if (StringUtils.isNotEmpty(output) || StringUtils.isNotEmpty(outputVrf)) {
            rtn.add(new ProtocolKey(OSPF.class, OSPF_NAME));
        }
        return rtn;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                             @Nonnull ProtocolBuilder protocolBuilder,
                                             @Nonnull ReadContext readContext) {
        ProtocolKey key = instanceIdentifier.firstKeyOf(Protocol.class);
        protocolBuilder.setName(key.getName());
        protocolBuilder.setIdentifier(key.getIdentifier());
    }

    public static String resolveVrfWithName(InstanceIdentifier<?> iid) {
        String vrfId = iid.firstKeyOf(NetworkInstance.class).getName();

        String rtn = NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)
                ?
                "" : " routing-instances " + vrfId;
        return rtn;
    }
}

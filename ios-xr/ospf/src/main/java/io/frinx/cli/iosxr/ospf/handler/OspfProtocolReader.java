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

package io.frinx.cli.iosxr.ospf.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.common.CompositeListReader;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
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

    private static final String SH_RUN_OSPF = "show running-config router ospf | include ^router ospf";
    private static final String SH_RUN_OSPF_PER_VRF = "show running-config router ospf %s %s";
    private static final Pattern ROUTER_OSPF_LINE = Pattern.compile("router ospf (?<ospfName>\\S+)");

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext)
            throws ReadFailedException {
        String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class)
                .getName();

        String output = blockingRead(SH_RUN_OSPF, cli, instanceIdentifier, readContext);

        List<ProtocolKey> keys = parseOspfIds(output);
        List<ProtocolKey> rtn = new ArrayList<>();

        for (ProtocolKey key : keys) {
            if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)) {
                rtn.add(key);
            } else {
                String detailShow = String.format(SH_RUN_OSPF_PER_VRF, key.getName(), "vrf " + vrfId);
                output = blockingRead(detailShow, cli, instanceIdentifier, readContext);
                if (StringUtils.isNotEmpty(output)) {
                    rtn.add(key);
                }
            }
        }
        return rtn;
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

    public static String resolveVrfWithName(InstanceIdentifier<?> iid) {
        String vrfId = iid.firstKeyOf(NetworkInstance.class).getName();

        String rtn = NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)
                ?
                "" : " vrf " + vrfId;
        return rtn;
    }
}

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

package io.frinx.cli.iosxr.ospfv3.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospfv3.OspfV3Reader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF3;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfV3ProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        OspfV3Reader.OspfV3ConfigReader<Protocol, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {

    private static final String SH_RUN_OSPFV3 = "show running-config router ospfv3 | include ^router ospfv3";
    private static final String SH_RUN_OSPFV3_PER_VRF = "show running-config router ospfv3 %s %s";
    private static final Pattern ROUTER_OSPFV3_LINE = Pattern.compile("router ospfv3 (?<ospfv3Name>\\S+)");

    private Cli cli;

    public OspfV3ProtocolReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> instanceIdentifier,
                                       @Nonnull ReadContext readContext)
            throws ReadFailedException {
        final String vrfId = instanceIdentifier.firstKeyOf(NetworkInstance.class)
                .getName();

        String output = blockingRead(SH_RUN_OSPFV3, cli, instanceIdentifier, readContext);

        List<ProtocolKey> keys = parseOspfV3Ids(output);
        List<ProtocolKey> rtn = new ArrayList<>();

        for (ProtocolKey key : keys) {
            if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)) {
                rtn.add(key);
            } else {
                String detailShow = String.format(SH_RUN_OSPFV3_PER_VRF, key.getName(), "vrf " + vrfId);
                output = blockingRead(detailShow, cli, instanceIdentifier, readContext);
                if (StringUtils.isNotEmpty(output)) {
                    rtn.add(key);
                }
            }
        }

        return rtn;
    }

    @VisibleForTesting
    public static List<ProtocolKey> parseOspfV3Ids(String output) {
        return ParsingUtils.parseFields(output, 0,
                ROUTER_OSPFV3_LINE::matcher,
            matcher -> matcher.group("ospfv3Name"),
            s -> new ProtocolKey(OSPF3.class, s));
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

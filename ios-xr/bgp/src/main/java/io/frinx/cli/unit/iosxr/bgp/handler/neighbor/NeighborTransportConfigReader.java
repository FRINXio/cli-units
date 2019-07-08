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

package io.frinx.cli.unit.iosxr.bgp.handler.neighbor;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.BgpCommonNeighborGroupTransportConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.transport.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborTransportConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_NEI = "show running-config router bgp %s %s %s neighbor %s";
    private static final Pattern UPADTE_SOURCE_LINE = Pattern.compile("update-source (?<iface>.+)");

    private Cli cli;

    public NeighborTransportConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull
            ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orNull();

        if (globalConfig
                == null) {
            return;
        }

        IpAddress neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress();
        String address = new String(neighborIp.getValue());
        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(NetworInstance.DEFAULT_NETWORK_NAME) ? "" : "instance "
                + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SH_NEI, globalConfig.getAs()
                .getValue()
                .intValue(), insName, nwInsName, address), cli, instanceIdentifier, readContext);

        ParsingUtils.parseField(output.trim(), 0, UPADTE_SOURCE_LINE::matcher, matcher -> matcher.group("iface"),
            iface -> configBuilder.setLocalAddress(new BgpCommonNeighborGroupTransportConfig.LocalAddress(iface)));
    }
}

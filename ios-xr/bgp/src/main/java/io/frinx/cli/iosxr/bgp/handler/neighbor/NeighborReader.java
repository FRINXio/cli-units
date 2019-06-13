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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.BgpProtocolReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements CliConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private static final String SH_NEI = "show running-config router bgp %s %s %s | include ^%sneighbor";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("neighbor (?<neighborIp>.+)");

    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list)
                .build();
    }

    @Override
    public List<NeighborKey> getAllIds(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier, @Nonnull
            ReadContext readContext) throws ReadFailedException {
        final String protName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        final String instance = BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(protName)
                ? "" : String.format("instance %s", protName);

        // TODO Same as in NeighborConfigReader
        final Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(Config.class))
                .orNull();

        if (globalConfig
                == null
                || globalConfig.getAs()
                == null) {
            // TODO Should we fail instead??
            return Collections.emptyList();
        }

        Long as = globalConfig.getAs()
                .getValue();

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        //indent is 1 when reading default config, otherwise it is 2.
        final String indent = nwInsName.isEmpty() ? " " : "  ";

        return getNeighborKeys(blockingRead(String.format(SH_NEI, as.intValue(), instance, nwInsName, indent), cli,
                instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier, @Nonnull
            NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress());
    }

    @VisibleForTesting
    public static List<NeighborKey> getNeighborKeys(String output) {
        return ParsingUtils.parseFields(output, 0, NEIGHBOR_LINE::matcher, matcher -> matcher.group("neighborIp"),
                // TODO Do not use IpAddress(char[] _value) constructor
            value -> new NeighborKey(new IpAddress(value.trim()
                        .toCharArray())));
    }
}

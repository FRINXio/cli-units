/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.huawei.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiReader implements BgpListReader.BgpConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {


    private static final Pattern FAMILY_LINE = Pattern.compile("\\s*ipv4-family (?<family>\\S+).*");
    private static final Pattern FAMILY_LINE_VRF = Pattern.compile("\\s*ipv4-family vpn-instance (?<vrf>\\S+).*");
    private Cli cli;

    public NeighborAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIdsForType(@Nonnull InstanceIdentifier<AfiSafi> id,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        String neighborIp = getNeighborIp(id);
        return getAfiKeys(blockingRead(String.format(NeighborConfigReader.DISPLAY_PEER_CONFIG, neighborIp), cli, id, readContext), vrfKey);
    }

    static String getNeighborIp(InstanceIdentifier<?> neigh) {
        IpAddress addr = neigh.firstKeyOf(Neighbor.class).getNeighborAddress();
        return getNeighborIp(addr);
    }

    static String getNeighborIp(IpAddress addr) {
        return addr.getIpv4Address() != null ?
                addr.getIpv4Address().getValue() :
                addr.getIpv6Address().getValue();
    }

    @VisibleForTesting
    static List<AfiSafiKey> getAfiKeys(String output, NetworkInstanceKey vrfKey) {
        output = output.replaceAll("\\n|\\r", "");
        output = output.replaceAll("ipv4-family", "\nipv4-family");

        String vrfName = vrfKey.getName();
        if (NetworInstance.DEFAULT_NETWORK.equals(vrfKey)) {
            return ParsingUtils.NEWLINE.splitAsStream(output)
                    // Skip header line(s)
                    .map(String::trim)
                    .filter(line -> !line.contains("vpn-instance"))
                    .filter(line -> line.contains("enable"))
                    .map(FAMILY_LINE::matcher)
                    .filter(Matcher::matches)
                    .map(m -> m.group("family"))
                    .map(GlobalAfiSafiReader::transformAfi)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(AfiSafiKey::new)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            return ParsingUtils.NEWLINE.splitAsStream(output)
                    // Skip header line(s)
                    .map(String::trim)
                    .filter(line -> line.contains("enable"))
                    .map(FAMILY_LINE_VRF::matcher)
                    .filter(Matcher::matches)
                    .filter(m -> m.group("vrf").equals(vrfName))
                    .map(value -> new AfiSafiKey(IPV4UNICAST.class))
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder,
                      @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier,
                                             @Nonnull AfiSafiBuilder afiSafiBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(key).build());
    }
}

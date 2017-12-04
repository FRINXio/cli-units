/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.regex.Pattern;

public class AfiSafiReader implements BgpListReader.BgpConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String SH_AFI = "show bgp neighbor %s | begin For Address Family";
    private static final Pattern FAMILY_LINE = Pattern.compile("(.*)For Address Family: (?<family>[^\\n].*)");
    private Cli cli;

    public AfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIdsForType(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        String neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress().getIpv4Address().getValue();
        return getAfiKeys(blockingRead(String.format(SH_AFI, neighborIp), cli, instanceIdentifier, readContext));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Nonnull
    @Override
    public AfiSafiBuilder getBuilder(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier) {
        return new AfiSafiBuilder();
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull AfiSafiBuilder afiSafiBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(key).build());
    }

    @VisibleForTesting
    public static List<AfiSafiKey> getAfiKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
                FAMILY_LINE::matcher,
                matcher -> matcher.group("family"),
                value -> new AfiSafiKey(transformAfi(value.trim())));
    }

    private static Class<? extends AFISAFITYPE> transformAfi(String afi) {
        // FIXME: add more if necessary
        switch(afi) {
            case "IPv4 Unicast":
                return IPV4UNICAST.class;
            case "VPNv4 Unicast":
                return L3VPNIPV4UNICAST.class;
        }
        throw new IllegalArgumentException("Unknown AFI/SAFI type " + afi);
    }
}

/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.neighbor;

import static io.frinx.cli.io.Cli.NEWLINE;
import static io.frinx.cli.ios.bgp.handler.BgpProtocolReader.DEFAULT_BGP_INSTANCE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.NeighborsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborReader implements BgpListReader.BgpConfigListReader<Neighbor, NeighborKey, NeighborBuilder> {

    private static final String SH_SUMM = "sh run | include ^router bgp|^ *address-family|^ *neighbor";
    private static final Pattern NEIGHBOR_LINE = Pattern.compile("neighbor (?<neighborIp>\\S*) .*");
    private Cli cli;

    public NeighborReader(Cli cli) {
        this.cli = cli;
    }

    @Override public List<NeighborKey> getAllIdsForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
        @Nonnull ReadContext readContext) throws ReadFailedException {

        String networkInstanceName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (DEFAULT_BGP_INSTANCE.equals(networkInstanceName)) {
            return getDefaultNeighborKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext));
        } else {
            return getVrfNeighborKeys(blockingRead(SH_SUMM, cli, instanceIdentifier, readContext),
                networkInstanceName);
        }
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<Neighbor> list) {
        ((NeighborsBuilder) builder).setNeighbor(list).build();
    }

    @VisibleForTesting
    public static List<NeighborKey> getVrfNeighborKeys(String output, String vrfName) {
        Optional<String> optionalVrfOutput =
            Arrays.stream(getSplitedOutput(output)).filter(value -> value.contains(vrfName)).findFirst();

        if(optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll(" neighbor", "\n neighbor"), 0,
                NEIGHBOR_LINE::matcher,
                matcher -> matcher.group("neighborIp"),
                (String value) -> new NeighborKey(new IpAddress(value.toCharArray())));
        }
        return new ArrayList<>();
    }

    @Override public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Neighbor> instanceIdentifier,
        @Nonnull NeighborBuilder neighborBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        neighborBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
    }

    @VisibleForTesting
    public static List<NeighborKey> getDefaultNeighborKeys(String output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(getSplitedOutput(output)).filter(value -> !value.contains("vrf")).findFirst();

        if(optionalVrfOutput.isPresent()) {
            return ParsingUtils.parseFields(optionalVrfOutput.get().replaceAll(" neighbor", "\n neighbor"), 0,
                    NEIGHBOR_LINE::matcher,
                    matcher -> matcher.group("neighborIp"),
                    (String value) -> new NeighborKey(new IpAddress(value.toCharArray())));
        }
        return new ArrayList<>();
    }

    public static String[] getSplitedOutput(String output) {
        return output.replaceAll(NEWLINE, "").replaceAll("\r", "")
            .replaceAll(" address-family ipv4 vrf", "\n address-family ipv4 vrf")
            .split("\\n");
    }
}

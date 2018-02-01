/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler;

import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiReader implements BgpListReader.BgpConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String SH_AFI = "sh run | include ^router bgp|^ address-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("\\s*address-family (?<family>\\S+).*");
    private static final Pattern FAMILY_VRF_LINE = Pattern.compile("\\s*address-family (?<family>.+) vrf (?<vrf>\\S+).*");
    private Cli cli;

    public GlobalAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIdsForType(@Nonnull InstanceIdentifier<AfiSafi> id,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();

        String output = blockingRead(SH_AFI, cli, id, readContext);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            return getDefaultAfiKeys(output);
        } else {
            return getAfiKeys(output, vrfName);
        }
    }

    private static String realignOutput(String output) {
        output = output.replaceAll("\\n|\\r", "");
        output = output.replace("router bgp ", "\nrouter bgp");
        output = NEWLINE.splitAsStream(output)
                .map(String::trim)
                .filter(s -> s.startsWith("router bgp"))
                .findFirst()
                .orElse("");

        output = output.replace("address-family", "\naddress-family");
        return output;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier,
                                             @Nonnull AfiSafiBuilder afiSafiBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
    }

    @VisibleForTesting
    static List<AfiSafiKey> getAfiKeys(String output, String vrf) {
        output = realignOutput(output);

        return NEWLINE.splitAsStream(output)
                // Skip header line(s)
                .map(String::trim)
                .map(FAMILY_VRF_LINE::matcher)
                .filter(Matcher::matches)
                .filter(m -> m.group("vrf").equals(vrf))
                .map(m -> m.group("family"))
                .map(s -> transformAfi(s.trim()))
                .filter(Optional::isPresent)
                .map(value -> new AfiSafiKey(value.get()))
                .distinct()
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static List<AfiSafiKey> getDefaultAfiKeys(String output) {
        output = realignOutput(output);

        return NEWLINE.splitAsStream(output)
                // Skip header line(s)
                .map(String::trim)
                .map(FAMILY_LINE::matcher)
                .filter(Matcher::matches)
                .map(m -> m.group("family"))
                .map(s -> transformAfi(s.trim()))
                .filter(Optional::isPresent)
                .map(value -> new AfiSafiKey(value.get()))
                .distinct()
                .collect(Collectors.toList());
    }

    public static Optional<Class<? extends AFISAFITYPE>> transformAfi(String afi) {
        switch (afi) {
            case "ipv4":
                return Optional.of(IPV4UNICAST.class);
            case "vpnv4":
                return Optional.of(L3VPNIPV4UNICAST.class);
            case "vpnv6":
                return Optional.of(L3VPNIPV6UNICAST.class);
            case "ipv6":
                return Optional.of(IPV6UNICAST.class);
        }

        return Optional.empty();
    }
}

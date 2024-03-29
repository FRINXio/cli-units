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

package io.frinx.cli.unit.huawei.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String DISPLAY_AFI_CONFIG =
            "display current-configuration configuration bgp | include ^ ipv4-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("\\s*ipv4-family (?<family>vpnv4|unicast).*");
    private static final Pattern FAMILY_VRF_LINE = Pattern.compile("\\s*ipv4-family vpn-instance (?<vrf>\\S+).*");

    private Cli cli;

    public GlobalAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> id,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);
        String vrfName = vrfKey.getName();

        String output = blockingRead(DISPLAY_AFI_CONFIG, cli, id, readContext);

        if (vrfKey.equals(NetworInstance.DEFAULT_NETWORK)) {
            return getDefaultAfiKeys(output);
        } else {
            return getAfiKeys(output, vrfName);
        }
    }

    private static String realignOutput(String output) {
        String withoutNewlines = output.replaceAll(ParsingUtils.NEWLINE.pattern(), "");
        withoutNewlines = withoutNewlines.replace("ipv4-family", "\nipv4-family");
        return withoutNewlines;
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> parentBuilder, @NotNull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier,
                                             @NotNull AfiSafiBuilder afiSafiBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
    }

    @VisibleForTesting
    static List<AfiSafiKey> getAfiKeys(String output, String vrf) {
        output = realignOutput(output);

        return ParsingUtils.NEWLINE.splitAsStream(output)
                // Skip header line(s)
                .map(String::trim)
                .map(FAMILY_VRF_LINE::matcher)
                .filter(Matcher::matches)
                .filter(m -> m.group("vrf").equals(vrf))
                .map(value -> new AfiSafiKey(IPV4UNICAST.class))
                .collect(Collectors.toList());
    }

    @VisibleForTesting
    static List<AfiSafiKey> getDefaultAfiKeys(String output) {
        output = realignOutput(output);

        return ParsingUtils.NEWLINE.splitAsStream(output)
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
            case "unicast":
                return Optional.of(IPV4UNICAST.class);
            case "vpnv4":
                return Optional.of(L3VPNIPV4UNICAST.class);
            default: break;
        }
        return Optional.empty();
    }
}
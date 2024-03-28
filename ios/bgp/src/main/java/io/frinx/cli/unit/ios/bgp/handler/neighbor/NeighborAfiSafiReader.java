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

package io.frinx.cli.unit.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ios.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {


    private static final Pattern FAMILY_LINE = Pattern.compile("\\s*address-family (?<family>\\S+).*");
    private static final Pattern FAMILY_LINE_VRF = Pattern.compile("\\s*address-family (?<family>\\S+) vrf "
            + "(?<vrf>\\S+).*");
    private Cli cli;

    public NeighborAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> id,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        NetworkInstanceKey vrfKey = id.firstKeyOf(NetworkInstance.class);

        String neighborIp = NeighborWriter.getNeighborIp(id);
        return getAfiKeys(blockingRead(String.format(NeighborConfigReader.SH_SUMM, neighborIp), cli, id, readContext),
                vrfKey,
            line -> line.contains("activate"));
    }

    @VisibleForTesting
    public static List<AfiSafiKey> getAfiKeys(String output, NetworkInstanceKey vrfKey, Predicate<String>
            filterActiveFamilies) {
        output = output.replaceAll("[\\n\\r]", "");
        output = output.replaceAll("address-family", "\naddress-family");

        String vrfName = vrfKey.getName();
        if (NetworInstance.DEFAULT_NETWORK.equals(vrfKey)) {
            return ParsingUtils.NEWLINE.splitAsStream(output)
                    // Skip header line(s)
                    .map(String::trim)
                    .filter(line -> !line.contains("vrf"))
                    .filter(filterActiveFamilies)
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
                    .filter(filterActiveFamilies)
                    .map(FAMILY_LINE_VRF::matcher)
                    .filter(Matcher::matches)
                    .filter(m -> m.group("vrf").equals(vrfName))
                    .map(m -> m.group("family"))
                    .map(GlobalAfiSafiReader::transformAfi)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(AfiSafiKey::new)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier,
                                             @NotNull AfiSafiBuilder afiSafiBuilder,
                                             @NotNull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(key).build());
    }
}
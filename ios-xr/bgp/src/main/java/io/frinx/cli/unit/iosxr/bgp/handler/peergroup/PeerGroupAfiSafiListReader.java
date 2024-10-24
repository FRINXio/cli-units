/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupAfiSafiListReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    static final String SH_AFI = "show running-config router bgp %s %s %s"
        + " neighbor-group %s | include address-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("address-family (?<family>.+)");
    private Cli cli;

    public PeerGroupAfiSafiListReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> iid, @NotNull
            ReadContext context) throws ReadFailedException {
        String peerGroupName = iid.firstKeyOf(PeerGroup.class).getPeerGroupName();
        Long as = PeerGroupListReader.readAsNumberFromContext(iid, context);
        if (as == null) {
            return Collections.emptyList();
        }
        final String protName = iid.firstKeyOf(Protocol.class).getName();
        final String instance = BgpProtocolReader.DEFAULT_BGP_INSTANCE.equals(protName)
                ? "" : String.format("instance %s", protName);
        String nwInsName = GlobalConfigWriter.resolveVrfWithName(iid);
        String cmd = f(SH_AFI, as,  instance, nwInsName, peerGroupName);
        String output = blockingRead(cmd, cli, iid, context);
        return parseAllIds(output);
    }

    @VisibleForTesting
    public List<AfiSafiKey> parseAllIds(String output) {
        return ParsingUtils.parseFields(
            output,
            0,
            FAMILY_LINE::matcher,
            matcher -> matcher.group("family"),
            value -> GlobalAfiSafiReader.transformAfiFromString(value.trim()))
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(AfiSafiKey::new)
            .collect(Collectors.toList());
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> iid, @NotNull
            AfiSafiBuilder afiSafiBuilder, @NotNull ReadContext context) {
        Class<? extends AFISAFITYPE> cla = iid.firstKeyOf(AfiSafi.class).getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(cla);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(cla).build());
    }
}
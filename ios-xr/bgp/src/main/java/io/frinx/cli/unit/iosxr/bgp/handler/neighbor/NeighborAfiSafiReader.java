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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.BgpProtocolReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeAfAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeAfAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.soft.reconfiguration.group.SoftReconfigurationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String SH_AFI = "show running-config router bgp %s %s %s neighbor %s | include address-family";
    private static final String SH_AFI_SECTION = "show running-config router bgp %s %s %s "
            + "neighbor %s address-family %s";
    private static final Pattern FAMILY_LINE = Pattern.compile("(.*)address-family (?<family>[^\\n].*)");
    private static final Pattern SOFT_RECONFIGURATION_LINE = Pattern.compile("soft-reconfiguration inbound(?<always> "
            + "always)*");
    private Cli cli;

    public NeighborAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull
            ReadContext readContext) throws ReadFailedException {
        final Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(Config.class))
                .orElse(null);

        if (globalConfig == null) {
            return Collections.EMPTY_LIST;
        }

        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(BgpProtocolReader.DEFAULT_BGP_INSTANCE)
                ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);

        String neighborIp = new String(instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress()
                .getValue());

        return getAfiKeys(blockingRead(String.format(SH_AFI, globalConfig.getAs()
                .getValue(), insName, nwInsName, neighborIp), cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull
            AfiSafiBuilder afiSafiBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        final Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(Config.class))
                .orElse(null);

        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(NetworInstance.DEFAULT_NETWORK_NAME)
                ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);

        String neighborIp = new String(instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress()
                .getValue());
        Class<? extends AFISAFITYPE> name = instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName();

        String output = blockingRead(String.format(SH_AFI_SECTION, globalConfig.getAs()
                        .getValue(), insName, nwInsName, neighborIp,
                GlobalAfiSafiReader.transformAfiToString(name)), cli, instanceIdentifier, readContext);
        Optional<Matcher> reconfigMatch = ParsingUtils.NEWLINE.splitAsStream(output.trim())
                .map(SOFT_RECONFIGURATION_LINE::matcher)
                .filter(Matcher::find)
                .findAny();


        afiSafiBuilder.setAfiSafiName(name);
        ConfigBuilder configBuilder = new ConfigBuilder().setAfiSafiName(name);
        if (reconfigMatch.isPresent()) {
            configBuilder.addAugmentation(BgpNeAfAug.class, new BgpNeAfAugBuilder()
                    .setSoftReconfiguration(new SoftReconfigurationBuilder()
                            .setAlways(reconfigMatch.get()
                                    .group("always") != null)
                            .build())
                    .build());
        }

        afiSafiBuilder.setConfig(configBuilder.build());
    }

    @VisibleForTesting
    public static List<AfiSafiKey> getAfiKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            FAMILY_LINE::matcher, matcher -> matcher.group("family"),
            value -> GlobalAfiSafiReader.transformAfiFromString(value.trim()).map(AfiSafiKey::new))
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
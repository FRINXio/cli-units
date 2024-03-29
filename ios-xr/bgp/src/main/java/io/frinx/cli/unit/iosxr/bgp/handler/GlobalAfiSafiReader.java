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

package io.frinx.cli.unit.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4LABELEDUNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6LABELEDUNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiReader implements CliConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String SH_AFI = "show running-config router bgp %s %s %s | include ^%saddress-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("(.*)address-family (?<family>[^\\n].*)");
    private Cli cli;

    public GlobalAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @NotNull
    @Override
    public List<AfiSafiKey> getAllIds(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull
            ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orElse(null);

        if (globalConfig == null) {
            return Collections.EMPTY_LIST;
        }
        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(NetworInstance.DEFAULT_NETWORK_NAME)
                ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        //indent is 1 when reading default config, otherwise it is 2.
        final String indent = nwInsName.isEmpty() ? " " : "  ";

        return getAfiKeys(blockingRead(String.format(SH_AFI, globalConfig.getAs()
                .getValue(), insName, nwInsName, indent), cli, instanceIdentifier, readContext));
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<AfiSafi> instanceIdentifier, @NotNull
            AfiSafiBuilder afiSafiBuilder, @NotNull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> key = instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName();
        afiSafiBuilder.setAfiSafiName(key);
        afiSafiBuilder.setConfig(new ConfigBuilder().setAfiSafiName(key)
                .build());
    }

    @VisibleForTesting
    public static List<AfiSafiKey> getAfiKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            FAMILY_LINE::matcher,
            matcher -> matcher.group("family"),
            value -> transformAfiFromString(value.trim()).map(AfiSafiKey::new))
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    public static Optional<Class<? extends AFISAFITYPE>> transformAfiFromString(String afi) {
        // FIXME: add more if necessary
        switch (afi) {
            case "ipv4 unicast":
                return Optional.of(IPV4UNICAST.class);
            case "vpnv4 unicast":
                return Optional.of(L3VPNIPV4UNICAST.class);
            case "ipv6 unicast":
                return Optional.of(IPV6UNICAST.class);
            case "vpnv6 unicast":
                return Optional.of(L3VPNIPV6UNICAST.class);
            case "ipv6 labeled-unicast":
                return Optional.of(IPV6LABELEDUNICAST.class);
            case "ipv4 labeled-unicast":
                return Optional.of(IPV4LABELEDUNICAST.class);
            default: break;

        }
        return Optional.empty();
    }

    public static String transformAfiToString(Class<? extends AFISAFITYPE> afi) {
        // FIXME: add more if necessary
        if (IPV4UNICAST.class.equals(afi)) {
            return "ipv4 unicast";
        } else if (L3VPNIPV4UNICAST.class.equals(afi)) {
            return "vpnv4 unicast";
        } else if (IPV6UNICAST.class.equals(afi)) {
            return "ipv6 unicast";
        } else if (L3VPNIPV6UNICAST.class.equals(afi)) {
            return "vpnv6 unicast";
        } else if (IPV4LABELEDUNICAST.class.equals(afi)) {
            return "ipv4 labeled-unicast";
        } else if (IPV6LABELEDUNICAST.class.equals(afi)) {
            return "ipv6 labeled-unicast";
        }

        throw new IllegalArgumentException("Unknown AFI/SAFI type " + afi);
    }
}
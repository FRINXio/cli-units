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

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.bgp.BgpListReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.afi.safi.list.afi.safi.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.AfiSafisBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.L3VPNIPV6UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalAfiSafiReader implements BgpListReader.BgpConfigListReader<AfiSafi, AfiSafiKey, AfiSafiBuilder> {

    private static final String SH_AFI = "show running-config router bgp %s %s | include ^ address-family";
    private static final Pattern FAMILY_LINE = Pattern.compile("(.*)address-family (?<family>[^\\n].*)");
    private Cli cli;

    public GlobalAfiSafiReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<AfiSafiKey> getAllIdsForType(@Nonnull InstanceIdentifier<AfiSafi> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config.class))
                .orNull();

        if (globalConfig == null) {
            return Collections.EMPTY_LIST;
        }
        String insName = instanceIdentifier.firstKeyOf(Protocol.class).getName().equals(NetworInstance.DEFAULT_NETWORK_NAME) ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class).getName();
        return getAfiKeys(blockingRead(String.format(SH_AFI, globalConfig.getAs().getValue(), insName), cli, instanceIdentifier, readContext));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull List<AfiSafi> readValue) {
        ((AfiSafisBuilder) parentBuilder).setAfiSafi(readValue);
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
                value -> new AfiSafiKey(transformAfiFromString(value.trim())));
    }

    public static Class<? extends AFISAFITYPE> transformAfiFromString(String afi) {
        // FIXME: add more if necessary
        switch(afi) {
            case "ipv4 unicast":
                return IPV4UNICAST.class;
            case "vpnv4 unicast":
                return L3VPNIPV4UNICAST.class;
            case "ipv6 unicast":
                return IPV6UNICAST.class;
            case "vpnv6 unicast":
                return L3VPNIPV6UNICAST.class;
        }
        throw new IllegalArgumentException("Unknown AFI/SAFI type " + afi);
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
        }
        throw new IllegalArgumentException("Unknown AFI/SAFI type " + afi);
    }
}

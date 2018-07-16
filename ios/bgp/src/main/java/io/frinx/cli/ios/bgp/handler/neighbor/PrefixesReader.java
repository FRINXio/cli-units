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

package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.afi.safi.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.Prefixes;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.state.PrefixesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PrefixesReader implements BgpReader.BgpOperReader<Prefixes, PrefixesBuilder> {

    private static final String SH_IPV4 = "show bgp ipv4 unicast summary | section %s";
    private static final String SH_VPNV4 = "show bgp vpnv4 unicast all summary | section %s";

    private final Cli cli;

    public PrefixesReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Prefixes> instanceIdentifier,
                                             @Nonnull PrefixesBuilder prefixesBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        Class<? extends AFISAFITYPE> afiKey = instanceIdentifier.firstKeyOf(AfiSafi.class).getAfiSafiName();
        String neighborIp = NeighborWriter.getNeighborIp(instanceIdentifier);
        String command = afiKey.equals(IPV4UNICAST.class) ? String.format(SH_IPV4, neighborIp) : String.format(
                SH_VPNV4, neighborIp);
        parsePrefixes(blockingRead(command, cli, instanceIdentifier, readContext), prefixesBuilder, neighborIp);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Prefixes prefixes) {
        ((StateBuilder) builder).setPrefixes(prefixes);
    }

    @VisibleForTesting
    public static void parsePrefixes(String output, PrefixesBuilder builder, String neighborIp) {
        String pfx = NeighborStateReader.parseState(output, neighborIp);
        if (StringUtils.isNumeric(pfx)) {
            builder.setReceived(Long.valueOf(pfx));
        }
    }
}

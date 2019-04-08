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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.ipv4.ipv6.unicast.common.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.mp.ipv4.ipv6.unicast.common.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.AFISAFITYPE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public abstract class NeighborAfiSafiIpvConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    private static final String SH_NEI = "show running-config router bgp %s %s %s neighbor %s address-family %s";
    private static final Pattern DEFAULT_ORIGINATE_LINE = Pattern.compile("default-originate");

    private Cli cli;

    public NeighborAfiSafiIpvConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public abstract void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config);

    protected abstract boolean isCorrectAfi(Class<? extends AFISAFITYPE> afiClass);

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config
                globalConfig = readContext.read(RWUtils.cutId(instanceIdentifier, Bgp.class)
                .child(Global.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base
                        .Config.class))
                .orNull();

        if (globalConfig == null) {
            return;
        }
        Class<? extends AFISAFITYPE> afiClass = instanceIdentifier.firstKeyOf(AfiSafi.class)
                .getAfiSafiName();
        // prevent creating ipv4 container under ipv6 address-family and vice versa
        if (!isCorrectAfi(afiClass)) {
            return;
        }

        IpAddress neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress();
        String address = new String(neighborIp.getValue());
        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(NetworInstance.DEFAULT_NETWORK_NAME)
                ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();
        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);
        String afiName = GlobalAfiSafiReader.transformAfiToString(afiClass);


        String output = blockingRead(String.format(SH_NEI, globalConfig.getAs()
                .getValue()
                .intValue(), insName, nwInsName, address, afiName), cli, instanceIdentifier, readContext);

        // default is disabled
        configBuilder.setSendDefaultRoute(false);
        ParsingUtils.parseField(output.trim(), 0,
                DEFAULT_ORIGINATE_LINE::matcher,
                Matcher::matches,
                configBuilder::setSendDefaultRoute);
    }
}

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

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.util.RWUtils;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.CommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.PRIVATEASREMOVEALL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.IpAddress;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {


    private static final String SH_NEI = "show running-config router bgp %s %s %s neighbor %s";
    private static final String PASSWORD_FORM = "Encrypted[%s]";
    private static final Pattern REMOTE_AS_LINE = Pattern.compile(".*remote-as (?<remoteAs>\\S+).*");
    private static final Pattern NEIGHBOR_LINE = Pattern.compile(".*use neighbor-group (?<group>\\S+).*");
    private static final Pattern PASSWORD_LINE = Pattern.compile(".*password encrypted (?<password>\\S+).*");
    private static final Pattern SHUTDOWN_LINE = Pattern.compile(".*shutdown.*");
    private static final Pattern DESCRIPTION_LINE = Pattern.compile(".*description (?<description>.+)");
    private static final Pattern SEND_COMMUNITY_LINE = Pattern.compile(".*send-community-ebgp.*");
    private static final Pattern REMOVE_AS_LINE = Pattern.compile(".*remove-private-AS.*");

    private Cli cli;

    public NeighborConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((NeighborBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
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

        IpAddress neighborIp = instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress();
        configBuilder.setNeighborAddress(neighborIp);

        String address = new String(neighborIp.getValue());
        String insName = instanceIdentifier.firstKeyOf(Protocol.class)
                .getName()
                .equals(NetworInstance.DEFAULT_NETWORK_NAME)
                ?
                "" : "instance " + instanceIdentifier.firstKeyOf(Protocol.class)
                .getName();

        String nwInsName = GlobalConfigWriter.resolveVrfWithName(instanceIdentifier);

        String output = blockingRead(String.format(SH_NEI, globalConfig.getAs()
                .getValue()
                .intValue(), insName, nwInsName, address), cli, instanceIdentifier, readContext);

        readNeighbor(output, configBuilder, address);
    }

    @VisibleForTesting
    public static void readNeighbor(final String output, final ConfigBuilder configBuilder, final String
            neighborAddress) {

        String neighbor = ParsingUtils.NEWLINE.splitAsStream(realignOutput(output))
                .filter(neighborLine -> neighborLine.contains(neighborAddress))
                .findFirst()
                .orElse("");

        // remote-as 65000
        ParsingUtils.parseField(neighbor.trim(), 0,
                REMOTE_AS_LINE::matcher,
            matcher -> matcher.group("remoteAs"),
            value -> configBuilder.setPeerAs(new AsNumber(GlobalConfigReader.readASNumber(value.trim()))));

        // shutdown (reverse the result, if we DO find the match, set to FALSE)
        ParsingUtils.findMatch(neighbor, SHUTDOWN_LINE, configBuilder::setEnabled);
        configBuilder.setEnabled(!configBuilder.isEnabled());

        // use neighbor-group iBGP
        ParsingUtils.parseField(neighbor, NEIGHBOR_LINE::matcher, matcher -> matcher.group("group"),
                configBuilder::setPeerGroup);

        // password
        ParsingUtils.parseField(output, PASSWORD_LINE::matcher,
            matcher -> matcher.group("password"),
            password -> configBuilder.setAuthPassword(
                    new EncryptedPassword(new EncryptedString(String.format(PASSWORD_FORM, password)))));

        // description
        ParsingUtils.parseField(output, DESCRIPTION_LINE::matcher,
            matcher -> matcher.group("description"),
                configBuilder::setDescription);

        // send-community-ebgp
        ParsingUtils.parseField(output, SEND_COMMUNITY_LINE::matcher,
                Matcher::matches,
            matches -> configBuilder.setSendCommunity(matches ? CommunityType.BOTH : null));

        // remove-private-AS
        ParsingUtils.parseField(output, REMOVE_AS_LINE::matcher,
                Matcher::matches,
            matches -> configBuilder.setRemovePrivateAs(matches ? PRIVATEASREMOVEALL.class : null));
    }

    private static String realignOutput(String output) {
        String withoutNewlines = output.replaceAll("[\r\n]", "");
        return withoutNewlines.replace("neighbor ", "\n");
    }
}

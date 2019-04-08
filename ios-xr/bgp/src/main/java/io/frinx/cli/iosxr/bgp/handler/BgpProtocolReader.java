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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliListReader;
import io.frinx.openconfig.network.instance.NetworInstance;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.BGP;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class BgpProtocolReader implements CliListReader<Protocol, ProtocolKey, ProtocolBuilder>,
        CompositeListReader.Child<Protocol, ProtocolKey, ProtocolBuilder> {


    public static final String DEFAULT_BGP_INSTANCE = "default";

    private static final String SHOW_RUN_ROUTER_BGP = "show running-config router bgp | include ^router bgp";
    private static final Pattern BGP_INSTANCE_PATTERN
            = Pattern.compile("router bgp (?<as>[0-9]+(\\.([0-9]+))?) instance (?<instance>[\\S]+)");
    private static final Pattern DEFAULT_BGP_INSTANCE_PATTERN
            = Pattern.compile("router bgp (?<as>[0-9]+(\\.([0-9]+))?)");
    private static final String SH_RUN_BGP_PER_VRF = "show running-config router bgp %s %s %s";

    private final Cli cli;

    public BgpProtocolReader(final Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<ProtocolKey> getAllIds(@Nonnull InstanceIdentifier<Protocol> iid, @Nonnull ReadContext context)
            throws ReadFailedException {
        String nwInsName = iid.firstKeyOf(NetworkInstance.class).getName();

        String output = blockingRead(SHOW_RUN_ROUTER_BGP, cli, iid, context);

        List<AsAndInsName> aais = parseGbpProtocolKeys(output);
        List<ProtocolKey> rtn = new ArrayList<>();

        // grouping keys by network instance name
        for (AsAndInsName aai : aais) {
            String keyName = aai.key.getName();
            keyName = DEFAULT_BGP_INSTANCE.equals(keyName)
                    ? "" : "instance " + keyName;
            if (NetworInstance.DEFAULT_NETWORK_NAME.equals(nwInsName)) {
                rtn.add(aai.key); //adding all bgp instance to "default"
            } else {
                output = blockingRead(String.format(SH_RUN_BGP_PER_VRF, aai.asNumber, keyName, "vrf " + nwInsName),
                        cli,
                        iid, context);
                if (StringUtils.isNotEmpty(output)) {
                    rtn.add(aai.key);
                    break;
                }
            }
        }
        return rtn;
    }

    @VisibleForTesting
    public static class AsAndInsName {
        private String asNumber;
        private ProtocolKey key;

        public AsAndInsName(String asNumber, ProtocolKey key) {
            this.asNumber = asNumber;
            this.key = key;
        }

        public String getAsNumber() {
            return asNumber;
        }

        public ProtocolKey getKey() {
            return key;
        }
    }

    @VisibleForTesting
    public static List<AsAndInsName> parseGbpProtocolKeys(String output) {
        List<AsAndInsName> rtn = new ArrayList<>();
        String[] lines = output.split("\\r?\\n");
        for (String s : lines) {
            Matcher matche = BGP_INSTANCE_PATTERN.matcher(s);
            if (matche.find()) {
                AsAndInsName asAndInsName = new AsAndInsName(
                        GlobalConfigReader.readASNumber(matche.group("as")).toString(),
                        new ProtocolKey(BGP.class, matche.group("instance")));
                rtn.add(asAndInsName);
            } else {
                matche = DEFAULT_BGP_INSTANCE_PATTERN.matcher(s);
                if (matche.find()) {
                    AsAndInsName asAndInsName = new AsAndInsName(
                            GlobalConfigReader.readASNumber(matche.group("as")).toString(),
                            new ProtocolKey(BGP.class, DEFAULT_BGP_INSTANCE));
                    rtn.add(asAndInsName);
                }
            }
        }
        return rtn;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Protocol> iid,
                                             @Nonnull ProtocolBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        ProtocolKey key = iid.firstKeyOf(Protocol.class);
        builder.setName(key.getName());
        builder.setIdentifier(key.getIdentifier());
    }
}

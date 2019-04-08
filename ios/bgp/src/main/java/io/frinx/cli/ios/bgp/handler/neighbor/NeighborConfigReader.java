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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.CommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    public static final String SH_SUMM = "show running-config | include ^router bgp|^ *address-family|^ *neighbor %s";
    private static final String PASSWORD_FORM = "Encrypted[%s]";

    private static final Pattern NEIGHBOR_ACTIVATE_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) (?<enabled>activate).*");
    private static final Pattern REMOTE_AS_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) remote-as (?<remoteAs>\\S*).*");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) password (?<password>.+)");
    private static final Pattern PEER_GROUP_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) peer-group (?<peerGroup>.+)");
    private static final Pattern DESCRIPTION_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) description (?<description>.+)");
    private static final Pattern SEND_COMMUNITY_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) send-community (?<community>.+)");
    private static final Pattern PASSWORD_REGEX_FORM = Pattern.compile("^([\\d] )\\S+$");

    private final Cli cli;

    public NeighborConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier.firstKeyOf(Neighbor.class)
                .getNeighborAddress());

        configBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
        parseConfigAttributes(blockingRead(String.format(SH_SUMM, ipAddress), cli, instanceIdentifier, readContext),
                configBuilder, vrfName);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {

        String[] vrfSplit = NeighborReader.splitOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseDefault(ConfigBuilder configBuilder, String[] output) {
        Optional<String> defaultNetworkNeighbors = Arrays.stream(output)
                .filter(value -> !value.contains("vrf"))
                .reduce((s1, s2) -> s1 + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private static void setAttributes(ConfigBuilder configBuilder, String output) {
        setAs(configBuilder, output);
        setEnabled(configBuilder, output);
        setPasswd(configBuilder, output);
        setPeerGroup(configBuilder, output);
        setDescription(configBuilder, output);
        setCommunity(configBuilder, output);
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        if (optionalVrfOutput.isPresent()) {
            String defaultInstance = optionalVrfOutput.get();
            setAttributes(configBuilder, defaultInstance);
        }
    }

    private static void setDescription(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, DESCRIPTION_PATTERN::matcher,
            m -> findGroup(m, "description"),
            groupsHashMap -> configBuilder.setDescription(groupsHashMap.get("description")));
    }

    private static void setPasswd(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, PASSWORD_PATTERN::matcher,
            m -> findGroup(m, "password"),
            groupsHashMap -> configBuilder.setAuthPassword(getPassword(groupsHashMap.get("password"))));
    }

    private static void setPeerGroup(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, PEER_GROUP_PATTERN::matcher,
            m -> findGroup(m, "peerGroup"),
            groupsHashMap -> configBuilder.setPeerGroup(groupsHashMap.get("peerGroup")));
    }

    private static void setAs(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, REMOTE_AS_PATTERN::matcher,
            m -> findGroup(m, "remoteAs"),
            groupsHashMap -> configBuilder.setPeerAs(new AsNumber(Long.valueOf(groupsHashMap.get("remoteAs")))));
    }

    private static void setCommunity(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, SEND_COMMUNITY_PATTERN::matcher,
            m -> findGroup(m, "community"),
            groupsHashMap -> configBuilder.setSendCommunity(CommunityType.valueOf(groupsHashMap.get("community")
                        .toUpperCase())));
    }

    private static String preprocessOutput(String defaultInstance) {
        return defaultInstance
                .replaceAll(" neighbor", "\n neighbor")
                .replaceAll(" address-family", "\n address-family");
    }

    private static void setEnabled(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(preprocessOutput(defaultInstance), 0, NEIGHBOR_ACTIVATE_PATTERN::matcher,
            m -> findGroup(m, "enabled"),
            groupsHashMap -> configBuilder.setEnabled("activate".equals(groupsHashMap.get("enabled"))));
    }

    private static HashMap<String, String> findGroup(Matcher matcher, String group) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("neighborIp", matcher.group("neighborIp"));
        hashMap.put(group, matcher.group(group));

        return hashMap;
    }

    private static EncryptedPassword getPassword(String password) {
        if (PASSWORD_REGEX_FORM.matcher(password).matches()) {
            return new EncryptedPassword(new EncryptedString(String.format(PASSWORD_FORM, password)));
        }
        return new EncryptedPassword(new PlainString(password));
    }
}

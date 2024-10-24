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

package io.frinx.cli.unit.huawei.bgp.handler.neighbor;

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
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.BgpNeighborConfigExtension.Transport;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.extension.rev180323.bgp.neighbor.config.extension.TimerConfigurationBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.EncryptedPassword;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.openconfig.types.rev170113.PlainString;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigReader implements CliConfigReader<Config, ConfigBuilder> {

    static final String DISPLAY_PEER_CONFIG = "display current-configuration configuration bgp "
            + "| include ^router bgp|^ *ipv4-family|^ *peer %s";
    private static final String NEIGHBOR_IP = "neighborIp";
    private static final String ENABLED = "enabled";
    private static final String ACTIVATE = "activate";

    private static final Pattern NEIGHBOR_ACTIVATE_PATTERN =
            Pattern.compile("peer (?<neighborIp>\\S*) enable.*");
    private static final Pattern REMOTE_AS_PATTERN =
            Pattern.compile("peer (?<neighborIp>\\S*) as-number (?<remoteAs>\\S*).*");
    private static final Pattern DESCRIPTION_PATTERN =
            Pattern.compile("peer (?<neighborIp>\\S*) description (?<description>.+)");
    private static final Pattern TIMER_PATTERN = Pattern.compile("peer (?<neighborIp>\\S*) timer"
            + " (?<timerMode>keepalive|connect-retry) (?<timer1>\\d+) hold (?<timer2>\\d+).*");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("peer (?<neighborIp>\\S*) password cipher (?<password>.+)");
    private static final Pattern PATH_MTU_PATTERN =
            Pattern.compile("peer (?<neighborIp>\\S*) path-mtu (?<transport>auto-discovery).*");

    private final Cli cli;

    public NeighborConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@NotNull Builder<? extends DataObject> builder, @NotNull Config config) {
        ((NeighborBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributes(@NotNull InstanceIdentifier<Config> instanceIdentifier,
                                      @NotNull ConfigBuilder configBuilder,
                                      @NotNull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborAfiSafiReader.getNeighborIp(
                instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());

        configBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
        parseConfigAttributes(blockingRead(String.format(DISPLAY_PEER_CONFIG, ipAddress), cli, instanceIdentifier,
                readContext), configBuilder, vrfName);
    }

    @VisibleForTesting
    public static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {
        String[] vrfSplit = getSplitedOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseDefault(ConfigBuilder configBuilder, String[] output) {
        Optional<String> defaultNetworkNeighbors = Arrays.stream(output)
            .filter(value -> !value.contains("vpn-instance"))
            .reduce((s1, s2) -> s1 + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private static void setAttributes(ConfigBuilder configBuilder, String output) {
        setAs(configBuilder, output);
        setEnabled(configBuilder, output);
        setDescription(configBuilder, output);
        setPassword(configBuilder, output);
        setBgpNeighborConfigAugmentation(configBuilder, output);
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
            Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        if (optionalVrfOutput.isPresent()) {
            String defaultInstance = optionalVrfOutput.get();
            setAttributes(configBuilder, defaultInstance);
        }
    }

    private static void setBgpNeighborConfigAugmentation(ConfigBuilder configBuilder, String output) {
        BgpNeighborConfigAugBuilder configAugBuilder = new BgpNeighborConfigAugBuilder();
        setPathMtu(configAugBuilder, output);
        setTimer(configAugBuilder, output);

        if (!configAugBuilder.build().equals(new BgpNeighborConfigAugBuilder().build())) {
            configBuilder.addAugmentation(BgpNeighborConfigAug.class, configAugBuilder.build());
        }
    }

    private static void setAs(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"),
            0, REMOTE_AS_PATTERN::matcher, m -> findGroup(m, "remoteAs"),
            groupsHashMap -> configBuilder.setPeerAs(new AsNumber(Long.valueOf(groupsHashMap.get("remoteAs")))));
    }

    private static void setEnabled(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"),
                0, NEIGHBOR_ACTIVATE_PATTERN::matcher, NeighborConfigReader::resolveGroupsActivate,
            groupsHashMap -> configBuilder.setEnabled(ACTIVATE.equals(groupsHashMap.get(ENABLED))));
    }

    private static void setDescription(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"),
            0, DESCRIPTION_PATTERN::matcher, m -> findGroup(m, "description"),
            groupsHashMap -> configBuilder.setDescription(groupsHashMap.get("description")));
    }

    private static void setPassword(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"), 0, PASSWORD_PATTERN::matcher,
            m -> findGroup(m, "password"), groupsHashMap -> configBuilder.setAuthPassword(
                new EncryptedPassword(new PlainString(groupsHashMap.get("password")))));
    }

    private static void setPathMtu(BgpNeighborConfigAugBuilder configAugBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"),
            0, PATH_MTU_PATTERN::matcher, m -> findGroup(m, "transport"),
            groupsHashMap -> configAugBuilder.setTransport(getTransport(groupsHashMap.get("transport"))));
    }

    private static void setTimer(BgpNeighborConfigAugBuilder configAugBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" peer", "\n peer"), 0, TIMER_PATTERN::matcher,
            m -> findGroup(m, "timerMode", "timer1", "timer2"),
            groupsHashMap -> configAugBuilder.setTimerConfiguration(new TimerConfigurationBuilder()
                .setTimerMode(groupsHashMap.get("timerMode"))
                .setTimeBefore(Short.parseShort(groupsHashMap.get("timer1")))
                .setTimeAfter(Short.parseShort(groupsHashMap.get("timer2"))).build()));
    }

    private static HashMap<String, String> resolveGroupsActivate(Matcher matcher) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(NEIGHBOR_IP, matcher.group(NEIGHBOR_IP));
        hashMap.put(ENABLED, ACTIVATE);
        return hashMap;
    }

    private static HashMap<String, String> findGroup(Matcher matcher, String... groupNames) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(NEIGHBOR_IP, matcher.group(NEIGHBOR_IP));
        Arrays.stream(groupNames).forEach(groupName -> hashMap.put(groupName, matcher.group(groupName)));
        return hashMap;
    }

    private static Transport getTransport(String transport) {
        for (final Transport type: Transport.values()) {
            if (transport.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }

    public static String[] getSplitedOutput(String output) {
        return output.replaceAll(Cli.NEWLINE, "").replaceAll("\r", "")
                .replaceAll(" ipv4-family", "\n ipv4-family")
                .split("\\n");
    }
}
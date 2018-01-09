/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.neighbor;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
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
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.NeighborBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.types.inet.rev170403.AsNumber;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {

    static final String SH_SUMM = "sh run | include ^router bgp|^ *address-family|^ *neighbor %s";
    private static final String NEIGHBOR_IP = "neighborIp";
    private static final String ENABLED = "enabled";
    private static final String REMOTE_AS = "remoteAs";
    private static final String ACTIVATE = "activate";
    private static final Pattern NEIGHBOR_ACTIVATE_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) (?<enabled>activate).*");
    private static final Pattern REMOTE_AS_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) remote-as (?<remoteAs>\\S*).*");

    private final Cli cli;

    public NeighborConfigReader(Cli cli) {
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
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());

        configBuilder.setNeighborAddress(instanceIdentifier.firstKeyOf(Neighbor.class).getNeighborAddress());
        parseConfigAttributes(blockingRead(String.format(SH_SUMM, ipAddress), cli, instanceIdentifier, readContext),
                configBuilder, vrfName);
    }

    private void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {

        String[] vrfSplit = NeighborReader.getSplitedOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private void parseDefault(ConfigBuilder configBuilder, String[] output) {
        Optional<String> defaultNetworkNeighbors = Arrays.stream(output)
                .filter(value -> !value.contains("vrf"))
                .reduce((s, s2) -> s + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private void setAttributes(ConfigBuilder configBuilder, String output) {
        setAs(configBuilder, output);
        setEnabled(configBuilder, output);
    }

    private void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        if (optionalVrfOutput.isPresent()) {
            String defaultInstance = optionalVrfOutput.get();
            setAttributes(configBuilder, defaultInstance);
        }
    }

    private void setAs(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" neighbor", "\n neighbor"), 0, REMOTE_AS_PATTERN::matcher,
                NeighborConfigReader::resolveGroupsRemoteAs,
                groupsHashMap -> configBuilder.setPeerAs(new AsNumber(Long.valueOf(groupsHashMap.get(REMOTE_AS)))));
    }

    private void setEnabled(ConfigBuilder configBuilder, String defaultInstance) {
        ParsingUtils.parseFields(defaultInstance.replaceAll(" neighbor", "\n neighbor"), 0, NEIGHBOR_ACTIVATE_PATTERN::matcher,
                NeighborConfigReader::resolveGroupsActivate,
                groupsHashMap -> configBuilder.setEnabled(ACTIVATE.equals(groupsHashMap.get(ENABLED))));
    }

    private static HashMap<String, String> resolveGroupsActivate(Matcher m) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(NEIGHBOR_IP, m.group(NEIGHBOR_IP));
        hashMap.put(ENABLED, m.group(ENABLED));

        return hashMap;
    }

    private static HashMap<String, String> resolveGroupsRemoteAs(Matcher m) {
        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put(NEIGHBOR_IP, m.group(NEIGHBOR_IP));
        hashMap.put(REMOTE_AS, m.group(REMOTE_AS));

        return hashMap;
    }
}

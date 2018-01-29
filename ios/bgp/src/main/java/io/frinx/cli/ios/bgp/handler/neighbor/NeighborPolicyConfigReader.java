/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.bgp.handler.neighbor;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.bgp.BgpReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.network.instance.NetworInstance;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.ApplyPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.ConfigBuilder;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborPolicyConfigReader implements BgpReader.BgpConfigReader<Config, ConfigBuilder> {

    private static final Pattern NEIGHBOR_POLICY_IN_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) route-map (?<updateSource>\\S*) in.*");
    private static final Pattern NEIGHBOR_POLICY_OUT_PATTERN =
            Pattern.compile("neighbor (?<neighborIp>\\S*) route-map (?<updateSource>\\S*) out.*");

    private final Cli cli;

    public NeighborPolicyConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull Config config) {
        ((ApplyPolicyBuilder) builder).setConfig(config);
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                             @Nonnull ConfigBuilder configBuilder,
                                             @Nonnull ReadContext readContext) throws ReadFailedException {
        String vrfName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ipAddress = NeighborWriter.getNeighborIp(instanceIdentifier);

        parseConfigAttributes(blockingRead(String.format(NeighborConfigReader.SH_SUMM, ipAddress), cli, instanceIdentifier, readContext),
                configBuilder, vrfName);
    }

    @VisibleForTesting
    static void parseConfigAttributes(String output, ConfigBuilder configBuilder, String vrfName) {

        String[] vrfSplit = NeighborReader.getSplitedOutput(output);

        if (NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfName)) {
            parseDefault(configBuilder, vrfSplit);
        } else {
            parseVrf(configBuilder, vrfName, vrfSplit);
        }
    }

    private static void parseDefault(ConfigBuilder configBuilder, String[] output) {
        Optional<String> defaultNetworkNeighbors = Arrays.stream(output)
                .filter(value -> !value.contains("vrf"))
                .reduce((s, s2) -> s + s2);

        setAttributes(configBuilder, defaultNetworkNeighbors.orElse(""));
    }

    private static void setAttributes(ConfigBuilder configBuilder, String output) {
        setPolicies(configBuilder, output);
    }

    private static void setPolicies(ConfigBuilder configBuilder, String defaultInstance) {
        String processed = defaultInstance.replaceAll(" neighbor", "\n neighbor");

        List<String> inPolicies = ParsingUtils.parseFields(processed, 0, NEIGHBOR_POLICY_IN_PATTERN::matcher,
                m -> m.group("updateSource"),
                Function.identity());

        configBuilder.setImportPolicy(inPolicies);

        List<String> outPolicies = ParsingUtils.parseFields(processed, 0, NEIGHBOR_POLICY_OUT_PATTERN::matcher,
                m -> m.group("updateSource"),
                Function.identity());

        configBuilder.setExportPolicy(outPolicies);
    }

    private static void parseVrf(ConfigBuilder configBuilder, String vrfName, String[] output) {
        Optional<String> optionalVrfOutput =
                Arrays.stream(output).filter(value -> value.contains(vrfName)).findFirst();

        setAttributes(configBuilder, optionalVrfOutput.orElse(""));
    }
}

/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.routing.policy.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ExtCommunitySetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpExtCommunityType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstances;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.NetworkInstancesBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.L3VRF;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ExtCommunitySetReader implements CliConfigListReader<ExtCommunitySet, ExtCommunitySetKey, ExtCommunitySetBuilder> {

    static final String ROUTE_TARGET_EXPORT_SET = "-route-target-export-set";
    static final String ROUTE_TARGET_IMPORT_SET = "-route-target-import-set";
    static final String RT = "rt";
    private static final String DISPLAY_VRF_CONFIG = "display current-configuration configuration vpn-instance %s";
    private static final Pattern VRF_ID_ROUTE_TARGET_EXPORT = Pattern.compile("vpn-target (?<rt>[\\S].*) export-extcommunity");
    private static final Pattern VRF_ID_ROUTE_TARGET_IMPORT = Pattern.compile("vpn-target (?<rt>[\\S].*) import-extcommunity");
    static final Pattern VRF_ID_ROUTE_TARGET = Pattern.compile("(?<vrf>\\S*)-route-target-(?<direction>import|export)-set");

    private Cli cli;

    public ExtCommunitySetReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static List<ExtCommunitySetKey> parseExtCommunityIds(String output, String vrfName) {
        List<ExtCommunitySetKey> exportKeys = ParsingUtils.parseFields(output, 0,
                VRF_ID_ROUTE_TARGET_EXPORT::matcher,
                Matcher::matches,
                value -> new ExtCommunitySetKey(vrfName + ROUTE_TARGET_EXPORT_SET));

        List<ExtCommunitySetKey> importKeys = ParsingUtils.parseFields(output, 0,
                VRF_ID_ROUTE_TARGET_IMPORT::matcher,
                Matcher::matches,
                value -> new ExtCommunitySetKey(vrfName + ROUTE_TARGET_IMPORT_SET));

        List<ExtCommunitySetKey> extCommunitySetKeys = new ArrayList<>();
        extCommunitySetKeys.addAll(exportKeys);
        extCommunitySetKeys.addAll(importKeys);

        return extCommunitySetKeys;
    }

    @VisibleForTesting
    static Config parseConfig(String output, InstanceIdentifier<ExtCommunitySet> identifier) {
        ExtCommunitySetKey extCommunitySetKey = identifier.firstKeyOf(ExtCommunitySet.class);

        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.setExtCommunitySetName(extCommunitySetKey.getExtCommunitySetName());
        if (isExport(extCommunitySetKey.getExtCommunitySetName())) {
            setIds(output, configBuilder, VRF_ID_ROUTE_TARGET_EXPORT);
        } else {
            setIds(output, configBuilder, VRF_ID_ROUTE_TARGET_IMPORT);
        }

        return configBuilder.build();
    }

    private static void setIds(String output, ConfigBuilder configBuilder, Pattern vrfIdRouteTargetImport) {
        List<ExtCommunitySetConfig.ExtCommunityMember> extCommunityMembers;
        extCommunityMembers = ParsingUtils.parseFields(output, 0,
                vrfIdRouteTargetImport::matcher,
                matcher -> matcher.group(RT),
                value -> new ExtCommunitySetConfig.ExtCommunityMember(new BgpExtCommunityType(value)));
        configBuilder.setExtCommunityMember(extCommunityMembers);
    }

    static boolean isExport(String extCommunitySetName) {
        return extCommunitySetName.contains("export");
    }

    @VisibleForTesting
    static Optional<String> getVrfName(ExtCommunitySetKey key) {
        String extCommunitySetName = key.getExtCommunitySetName();
        Matcher matcher = VRF_ID_ROUTE_TARGET.matcher(extCommunitySetName);
        return matcher.matches() ? Optional.ofNullable(matcher.group("vrf")) : Optional.empty();
    }

    @VisibleForTesting
    static Optional<String> getVrfDirection(ExtCommunitySetKey key) {
        String extCommunitySetName = key.getExtCommunitySetName();
        Matcher matcher = VRF_ID_ROUTE_TARGET.matcher(extCommunitySetName);
        return matcher.matches() ? Optional.ofNullable(matcher.group("direction")) : Optional.empty();
    }

    @Nonnull
    @Override
    public List<ExtCommunitySetKey> getAllIds(@Nonnull InstanceIdentifier<ExtCommunitySet> id,
                                              @Nonnull ReadContext context) throws ReadFailedException {

        List<String> vrfs = getExistingVrfs(context.read(IIDs.NETWORKINSTANCES));

        List<ExtCommunitySetKey> extCommunitySetKeys = new ArrayList<>();
        for (String vrf : vrfs) {
            List<ExtCommunitySetKey> keys =
                    parseExtCommunityIds(blockingRead(String.format(DISPLAY_VRF_CONFIG, vrf), cli, id, context), vrf);
            extCommunitySetKeys.addAll(keys);
        }

        return extCommunitySetKeys;
    }

    static List<String> getExistingVrfs(com.google.common.base.Optional<NetworkInstances> read) {
        return read
                .or(new NetworkInstancesBuilder().setNetworkInstance(Collections.emptyList()).build())
                .getNetworkInstance()
                .stream()
                .filter(i -> i.getConfig().getType().equals(L3VRF.class))
                .map(NetworkInstance::getName)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<ExtCommunitySet> readData) {
        ((ExtCommunitySetsBuilder) builder).setExtCommunitySet(readData);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<ExtCommunitySet> id,
                                      @Nonnull ExtCommunitySetBuilder builder, @Nonnull ReadContext ctx) throws ReadFailedException {
        Optional<String> vrfName = getVrfName(id.firstKeyOf(ExtCommunitySet.class));
        if (vrfName.isPresent()) {
            builder.setKey(id.firstKeyOf(ExtCommunitySet.class));
            builder.setConfig(parseConfig(blockingRead(String.format(DISPLAY_VRF_CONFIG, vrfName.get()), cli, id, ctx), id));
        }
    }
}

/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.routing.policy.handlers;

import com.google.common.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ExtCommunitySetConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ExtCommunitySetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.BgpExtCommunityType;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.CliReader;
import io.frinx.cli.unit.utils.ParsingUtils;

public class ExtCommunitySetReader implements CliConfigListReader<ExtCommunitySet, ExtCommunitySetKey, ExtCommunitySetBuilder> {

    static final Pattern ROUTE_TARGET_EXPORT = Pattern.compile("(?<vrf>[\\S].*)-route-target-export([\\S].*)");
    static final String ROUTE_TARGET_EXPORT_SET = "-route-target-export-set";
    static final String ROUTE_TARGET_IMPORT_SET = "-route-target-import-set";
    static final String RT = "rt";
    private static final String SH_RUN_VRF = "sh run | include vrf";
    private static final String SH_RUN_VRF_ID = "sh run vrf %s";
    private static final Pattern VRF_ID_LINE = Pattern.compile("ip vrf (?<vrf>[\\S].*)");
    private static final Pattern VRF_ID_ROUTE_TARGET_EXPORT = Pattern.compile("route-target export (?<rt>[\\S].*)");
    private static final Pattern VRF_ID_ROUTE_TARGET_IMPORT = Pattern.compile("route-target import (?<rt>[\\S].*)");
    private static final Pattern VRF_ID_ROUTE_TARGET = Pattern.compile("(?<vrf>\\S.*)-route-target-\\S.*");
    private static final String VRF_GROUP = "vrf";
    private Cli cli;

    public ExtCommunitySetReader(Cli cli) {
        this.cli = cli;
    }

    @VisibleForTesting
    static List<String> parseVrfIds(String output) {
        return ParsingUtils.parseFields(output, 0,
            VRF_ID_LINE::matcher,
            m -> m.group(VRF_GROUP),
            String::new);
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

    private static boolean isExport(String extCommunitySetName) {
        return ROUTE_TARGET_EXPORT.matcher(extCommunitySetName).matches();
    }

    @VisibleForTesting
    static String getVrfName(ExtCommunitySetKey key) {
        String extCommunitySetName = key.getExtCommunitySetName();
        Matcher matcher = VRF_ID_ROUTE_TARGET.matcher(extCommunitySetName.trim());
        return matcher.matches() ? matcher.group(VRF_GROUP) : "";
    }

    @Nonnull @Override public List<ExtCommunitySetKey> getAllIds(@Nonnull InstanceIdentifier<ExtCommunitySet> id,
        @Nonnull ReadContext context) throws ReadFailedException {

        return getAllIds(id, context, this.cli, this);
    }

    private List<ExtCommunitySetKey> getAllIds(InstanceIdentifier<ExtCommunitySet> id, ReadContext context, Cli cli,
        CliReader reader) throws ReadFailedException {
        List<String> vrfIds = parseVrfIds(reader.blockingRead(SH_RUN_VRF, cli, id, context));

        List<ExtCommunitySetKey> extCommunitySetKeys = new ArrayList<>();
        vrfIds.forEach(vrfName -> {
            try {
                List<ExtCommunitySetKey> keys =
                    parseExtCommunityIds(reader.blockingRead(String.format(SH_RUN_VRF_ID, vrfName), cli, id, context),
                        vrfName);
                extCommunitySetKeys.addAll(keys);

            } catch (ReadFailedException e) {
                LOG.warn("Failed to read ExtCommunityIds.", e);
            }
        });
        return extCommunitySetKeys;
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<ExtCommunitySet> readData) {
        ((ExtCommunitySetsBuilder) builder).setExtCommunitySet(readData);
    }

    @Nonnull @Override public ExtCommunitySetBuilder getBuilder(@Nonnull InstanceIdentifier<ExtCommunitySet> id) {
        return new ExtCommunitySetBuilder();
    }

    @Override public void readCurrentAttributes(@Nonnull InstanceIdentifier<ExtCommunitySet> id,
        @Nonnull ExtCommunitySetBuilder builder, @Nonnull ReadContext ctx) throws ReadFailedException {

        String vrfName = getVrfName(id.firstKeyOf(ExtCommunitySet.class));
        builder.setKey(id.firstKeyOf(ExtCommunitySet.class));
        builder.setConfig(parseConfig(this.blockingRead(String.format(SH_RUN_VRF_ID, vrfName), cli, id, ctx),
            id));
    }
}

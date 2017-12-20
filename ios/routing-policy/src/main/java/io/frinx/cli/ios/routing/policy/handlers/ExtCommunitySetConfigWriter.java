/*
 * Copyright (c) 2017 Cisco Systems. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.routing.policy.handlers;

import static io.frinx.cli.ios.routing.policy.handlers.ExtCommunitySetReader.ROUTE_TARGET_EXPORT;

import java.util.regex.Matcher;

import javax.annotation.Nonnull;

import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;

public class ExtCommunitySetConfigWriter  implements CliWriter<Config> {

    // todo This now handles just one special case for cisco route-target-export/import.
    // It checks defined sets for special defined sets with name <vrf>-route-target-import-set or
    // <vrf>-route-target-export-set and parses vrfname (<vrf>) from it. This should be changed when routing-policy
    // is handled correctly. In correct way we should listnen on inter-instance-policies in network instances,
    // which refer to frinx-openconfig-routing-policy:routing-policy/policy-definitions. There we can find
    // ext-community-set which points to frinx-openconfig-routing-policy:routing-policy/defined-sets where we can
    // extract route-targets from ext-community-set/config.
    // See https://github.com/FRINXio/translation-units-docs/blob/master/Configuration%20datasets/network-instances/l3vpn/network_instance_l3vpn_bgp.md
    // for example.


    private static final String ROUTE_TARGET_TEMPLATE = "route-target %s %s\n";
    private static final String NO_ROUTE_TARGET_TEMPLATE = "no route-target %s %s\n";
    private static final String WRITE_TEMPLATE = "configure terminal\n" +
        "ip vrf %s\n" +
        "%s\n" +
        "exit\n" +
        "exit";
    private Cli cli;

    public ExtCommunitySetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
        @Nonnull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        String vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);
        String routeTargets = parseRouteTargets(dataAfter, extCommunitySetKey, true);

        if (!routeTargets.isEmpty()) {
            blockingWriteAndRead(cli, id, dataAfter, f(WRITE_TEMPLATE, vrfName, routeTargets));
        }
    }

    private String getRouteTargets(Config config, String direction, boolean add) {
        if (config == null || config.getExtCommunityMember() == null || config.getExtCommunityMember().isEmpty()) {
            return "";
        }
        StringBuilder commands = new StringBuilder();
        String template = add ? ROUTE_TARGET_TEMPLATE : NO_ROUTE_TARGET_TEMPLATE;
        config.getExtCommunityMember().forEach(extCommunityMember -> commands.append(
            String.format(template, direction, extCommunityMember.getBgpExtCommunityType().getString())));

        return commands.toString();
    }

    @Override public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
        @Nonnull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        String vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);
        String routeTargets = parseRouteTargets(dataBefore, extCommunitySetKey, false);

        if (!routeTargets.isEmpty()) {
            blockingDeleteAndRead(cli, id, f(WRITE_TEMPLATE, vrfName, routeTargets));
        }
    }

    private String parseRouteTargets(@Nonnull Config data, ExtCommunitySetKey extCommunitySetKey, boolean add) {
        Matcher matcher = ROUTE_TARGET_EXPORT.matcher(extCommunitySetKey.getExtCommunitySetName());
        String direction = matcher.matches() ? "export" : "import";
        return getRouteTargets(data, direction, add);
    }
}

/*
 * Copyright (c) 2018 Cisco Systems. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.unit.huawei.routing.policy.handler;

import static com.google.common.base.Preconditions.checkArgument;
import static io.frinx.cli.unit.huawei.routing.policy.handler.ExtCommunitySetReader.VRF_ID_ROUTE_TARGET;
import static io.frinx.cli.unit.huawei.routing.policy.handler.ExtCommunitySetReader.getExistingVrfs;
import static io.frinx.cli.unit.huawei.routing.policy.handler.ExtCommunitySetReader.getVrfDirection;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ExtCommunitySetConfigWriter implements CliWriter<Config> {

    // todo This now handles just one special case for cisco route-target-export/import.
    // It checks defined sets for special defined sets with name <vrf>-route-target-import-set or
    // <vrf>-route-target-export-set and parses vrfname (<vrf>) from it. This should be changed when routing-policy
    // is handled correctly. In correct way we should listnen on inter-instance-policies in network instances,
    // which refer to frinx-openconfig-routing-policy:routing-policy/policy-definitions. There we can find
    // ext-community-set which points to frinx-openconfig-routing-policy:routing-policy/defined-sets where we can
    // extract route-targets from ext-community-set/config.
    // See https://github.com/FRINXio/translation-units-docs/blob/master/Configuration%20datasets/network-instances/l3vpn/network_instance_l3vpn_bgp.md
    // for example.

    private static final String WRITE_TEMPLATE_2 = "system-view\n" +
            "ip vpn-instance {$vrf}\n" +
            "ipv4-family\n" +
            "{% loop in $config.ext_community_member as $community %}\n" +
            "{.if ($add) }{.else}undo {/if}" + "vpn-target {$community.bgp_ext_community_type.string} {$direction}\n" +
            "{% onEmpty %}" +
            "{% endloop %}" +
            "commit\n" +
            "return";

    private Cli cli;

    public ExtCommunitySetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataAfter,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        Optional<String> vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);
        checkArgument(vrfName.isPresent(),
                "Invalid ext community: %s. Expected communities are in format: %s",
                extCommunitySetKey.getExtCommunitySetName(), VRF_ID_ROUTE_TARGET.pattern());

        List<String> vrfs = getExistingVrfs(writeContext.readAfter(IIDs.NETWORKINSTANCES));
        checkArgument(vrfs.contains(vrfName.get()), "Vrf: %s does not exist, cannot configure ext communities", vrfName);

        String routeTargets = serializeRouteTargets(dataAfter, extCommunitySetKey, true, vrfName.get());
        blockingWriteAndRead(cli, id, dataAfter, routeTargets);
    }

    private String getRouteTargets(Config config, String direction, boolean add, String vrfName) {
        return fT(WRITE_TEMPLATE_2,
                "vrf", vrfName,
                "config", config,
                "add", add ? true : null,
                "direction", direction);
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter, @Nonnull WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id, @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        Optional<String> vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);

        if (vrfName.isPresent()) {
            String routeTargets = serializeRouteTargets(dataBefore, extCommunitySetKey, false, vrfName.get());
            blockingDeleteAndRead(cli, id, routeTargets);
        }
    }

    private String serializeRouteTargets(@Nonnull Config data, ExtCommunitySetKey extCommunitySetKey, boolean add, String vrfName) {
        Optional<String> direction = getVrfDirection(extCommunitySetKey);
        return getRouteTargets(data, direction.get(), add, vrfName);
    }
}

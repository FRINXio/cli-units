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

package io.frinx.cli.unit.huawei.routing.policy.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ExtCommunitySetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.policy.rev170730.ext.community.set.top.ext.community.sets.ext.community.set.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ExtCommunitySetConfigWriter implements CliWriter<Config> {

    // todo This now handles just one special case for huawei route-target-export/import.
    // It checks defined sets for special defined sets with name <vrf>-route-target-import-set or
    // <vrf>-route-target-export-set and parses vrfname (<vrf>) from it. This should be changed when routing-policy
    // is handled correctly. In correct way we should listnen on inter-instance-policies in network instances,
    // which refer to frinx-openconfig-routing-policy:routing-policy/policy-definitions. There we can find
    // ext-community-set which points to frinx-openconfig-routing-policy:routing-policy/defined-sets where we can
    // extract route-targets from ext-community-set/config.
    // See https://github.com/FRINXio/translation-units-docs/blob/master/Configuration%20datasets/network-instances/l3vpn/network_instance_l3vpn_bgp.md
    // for example.

    private static final String WRITE_TEMPLATE_2 = """
            system-view
            ip vpn-instance {$vrf}
            ipv4-family
            {% loop in $config.ext_community_member as $community %}
            {.if ($add) }{.else}undo {/if}vpn-target {$community.bgp_ext_community_type.string} {$direction}
            {% onEmpty %}{% endloop %}commit
            return""";

    private Cli cli;

    public ExtCommunitySetConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataAfter,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        Optional<String> vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);
        Preconditions.checkArgument(vrfName.isPresent(),
                "Invalid ext community: %s. Expected communities are in format: %s",
                extCommunitySetKey.getExtCommunitySetName(), ExtCommunitySetReader.VRF_ID_ROUTE_TARGET.pattern());

        List<String> vrfs = ExtCommunitySetReader.getExistingVrfs(writeContext.readAfter(IIDs.NETWORKINSTANCES));
        Preconditions.checkArgument(vrfs.contains(vrfName.get()),
                "Vrf: %s does not exist, cannot configure ext communities", vrfName);

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
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws
            WriteFailedException {
        // this is correct procedure here, there is no 'update' of route-target
        // Any route-target previously not in config will be added, any route-target missing from config will be
        // deleted.
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
                                        @NotNull WriteContext writeContext) throws WriteFailedException {
        ExtCommunitySetKey extCommunitySetKey = id.firstKeyOf(ExtCommunitySet.class);
        Optional<String> vrfName = ExtCommunitySetReader.getVrfName(extCommunitySetKey);

        if (vrfName.isPresent()) {
            String routeTargets = serializeRouteTargets(dataBefore, extCommunitySetKey, false, vrfName.get());
            blockingDeleteAndRead(cli, id, routeTargets);
        }
    }

    private String serializeRouteTargets(@NotNull Config data, ExtCommunitySetKey extCommunitySetKey, boolean add,
                                         String vrfName) {
        Optional<String> direction = ExtCommunitySetReader.getVrfDirection(extCommunitySetKey);
        return getRouteTargets(data, direction.get(), add, vrfName);
    }
}
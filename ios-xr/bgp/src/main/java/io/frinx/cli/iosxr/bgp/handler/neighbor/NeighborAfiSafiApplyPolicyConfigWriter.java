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

package io.frinx.cli.iosxr.bgp.handler.neighbor;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiApplyPolicyConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborAfiSafiApplyPolicyConfigWriter(Cli cli) {
        this.cli = cli;
    }

    static final String WRITE_NEIGHBOR_AFI_APPLY_POLICY = "router bgp {$as} {$instance} {$vrf}\n"
            + "neighbor {$address}\n"
            + "address-family {$afiSafi}\n"
            + "{% loop in $config.import_policy as $inPolicy}\n"
            + "{.if ($delete)}no {/if}route-policy {$inPolicy} in\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "{% loop in $config.export_policy as $outPolicy}\n"
            + "{.if ($outPolicy == $nexthopself) }"
            + "{.if ($delete)}no {/if}next-hop-self\n"
            + "{.else}"
            + "{.if ($delete)}no {/if}route-policy {$outPolicy} out\n"
            + "{/if}"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "root\n";

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config config,
                                              WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        blockingWriteAndRead(fT(WRITE_NEIGHBOR_AFI_APPLY_POLICY,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "address", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue()),
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(id.firstKeyOf(AfiSafi.class)
                        .getAfiSafiName()),
                "nexthopself", NeighborAfiSafiApplyPolicyConfigReader.NEXTHOPSELF_POLICY_NAME,
                "config", config),
                cli, id, config);
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        deleteCurrentAttributesForType(id, dataBefore, writeContext);
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext writeContext)
            throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get()
                .getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        blockingWriteAndRead(fT(WRITE_NEIGHBOR_AFI_APPLY_POLICY,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "address", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue()),
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(id.firstKeyOf(AfiSafi.class)
                        .getAfiSafiName()),
                "nexthopself", NeighborAfiSafiApplyPolicyConfigReader.NEXTHOPSELF_POLICY_NAME,
                "config", config,
                "delete", true),
                cli, id, config);
    }
}

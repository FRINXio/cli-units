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

package io.frinx.cli.unit.iosxr.bgp.handler.neighbor;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.routing.policy.rev170714.apply.policy.group.apply.policy.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborAfiSafiApplyPolicyConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public NeighborAfiSafiApplyPolicyConfigWriter(Cli cli) {
        this.cli = cli;
    }

    static final String WRITE_NEIGHBOR_AFI_APPLY_POLICY = """
            router bgp {$as} {$instance} {$vrf}
            neighbor {$address}
            address-family {$afiSafi}
            {% loop in $config.import_policy as $inPolicy}
            {.if ($delete)}no {/if}route-policy {$inPolicy} in
            {% onEmpty %}{% endloop %}{% loop in $config.export_policy as $outPolicy}
            {.if ($outPolicy == $nexthopself) }{.if ($delete)}no {/if}next-hop-self
            {.else}{.if ($delete)}no {/if}route-policy {$outPolicy} out
            {/if}{% onEmpty %}{% endloop %}root
            """;

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
                                       @NotNull WriteContext writeContext) throws WriteFailedException {
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
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config dataBefore,
            @NotNull Config dataAfter, @NotNull WriteContext writeContext) throws WriteFailedException {
        // this is fine, there is no update for policy, either the policy is configured or it is not
        deleteCurrentAttributes(id, dataBefore, writeContext);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> id, @NotNull Config config,
            @NotNull WriteContext writeContext) throws WriteFailedException {
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
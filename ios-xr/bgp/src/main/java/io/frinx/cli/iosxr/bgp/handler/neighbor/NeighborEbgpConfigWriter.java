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
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.common.structure.neighbor.group.ebgp.multihop.ebgp.multihop.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborEbgpConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public NeighborEbgpConfigWriter(Cli cli) {
        this.cli = cli;
    }

    static final String NEIGHBOR_EBGP = "router bgp {$as} {$instance} {$vrf}\n"
            + "neighbor {$address}\n"
            + "{% if ($enabled == TRUE) %}"
            + "{% if ($config.multihop_ttl) %}"
            + "ebgp-multihop {$config.multihop_ttl}\n"
            + "{% else %}"
            + "no ebgp-multihop\n"
            + "{% endif %}"
            + "{% else %}"
            + "no ebgp-multihop\n"
            + "{% endif %}"
            + "root\n";

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext
            writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        blockingWriteAndRead(fT(NEIGHBOR_EBGP, "as", g.getConfig()
                .getAs()
                .getValue(), "instance", instName, "vrf", nwInsName, "address",
                new String(id.firstKeyOf(Neighbor.class)
                .getNeighborAddress()
                .getValue()), "enabled", config.isEnabled(), "config", config), cli, id, config);
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        writeCurrentAttributesForType(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config config, WriteContext
            writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get()
                .getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        blockingDeleteAndRead(fT(NEIGHBOR_EBGP, "as", g.getConfig()
                .getAs()
                .getValue(), "instance", instName, "vrf", nwInsName, "address",
                new String(id.firstKeyOf(Neighbor.class)
                .getNeighborAddress()
                .getValue()), "enabled", false), cli, id);
    }
}

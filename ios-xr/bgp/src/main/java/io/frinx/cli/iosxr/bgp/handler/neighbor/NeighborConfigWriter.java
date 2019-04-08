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
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.AfiSafis;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.neighbor.list.Neighbor;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.Bgp;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.top.bgp.Global;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NeighborConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public NeighborConfigWriter(Cli cli) {
        this.cli = cli;
    }

    static final String NEIGHBOR_GLOBAL = "router bgp {$as} {$instance} {$vrf}\n"
            + "neighbor {$address}\n"
            + "{% if ($config.peer_as) %}"
            + "remote-as {$config.peer_as.value}\n"
            + "{% elseIf ($oldConfig.peer_as) %}"
            + "no remote-as\n"
            + "{% endif %}"
            + "{% if ($config.auth_password) %}"
            + "{% if ($config.auth_password.encrypted_string) %}"
            + "password encrypted {$config.auth_password.encrypted_string.value|s/^Encrypted\\[|\\]$/ /g|trim}\n"
            + "{% elseIf ($config.auth_password.plain_string) %}"
            + "password {$config.auth_password.plain_string.value}\n"
            + "{% endif %}"
            + "{% elseIf ($oldConfig.auth_password) %}"
            + "no password\n"
            + "{% endif %}"
            + "{% if ($config.description) %}"
            + "description {$config.description}\n"
            + "{% elseIf ($oldConfig.description) %}"
            + "no description\n"
            + "{% endif %}"
            + "{% if ($config.peer_group) %}"
            + "use neighbor-group {$config.peer_group}\n"
            + "{% elseIf ($oldConfig.peer_group) %}"
            + "no use neighbor-group\n"
            + "{% endif %}"
            + "{.if ($enabled == TRUE) }no shutdown\n{.else}shutdown\n{/if}"
            + "{% loop in $afiSafis as $afiSafi}\n"
            + "address-family {$afiSafi}\n"
            + "{% if ($config.send_community) %}"
            + "send-community-ebgp\n"
            + "{% elseIf ($oldConfig.send_community) %}"
            + "no send-community-ebgp\n"
            + "{% endif %}"
            + "{% if ($config.remove_private_as) %}"
            + "remove-private-AS\n"
            + "{% elseIf ($oldConfig.send_community) %}"
            + "no remove-private-AS\n"
            + "{% endif %}"
            + "exit\n"
            + "{% onEmpty %}"
            + "{% endloop %}"
            + "root\n";

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> id, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        Optional<AfiSafis> optionalAfiSafis = writeContext.readAfter(id.firstIdentifierOf(Neighbor.class)
                .child(AfiSafis.class));
        List<String> afiSafis = new ArrayList<>();
        if (optionalAfiSafis.isPresent()) {
            afiSafis = optionalAfiSafis.get()
                    .getAfiSafi()
                    .stream()
                    .map(afiSafi -> GlobalAfiSafiReader.transformAfiToString(afiSafi.getAfiSafiName()))
                    .collect(Collectors.toList());
        }
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);

        blockingWriteAndRead(fT(NEIGHBOR_GLOBAL,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "address", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue()),
                "afiSafis", afiSafis,
                "enabled", data.isEnabled(), //chunk can process only getters
                "config", data,
                "oldConfig", null),
                cli, id, data);
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        Preconditions.checkArgument(bgpOptional.isPresent());
        final Global g = Preconditions.checkNotNull(bgpOptional.get()
                .getGlobal());
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        Optional<AfiSafis> optionalAfiSafis = writeContext.readAfter(id.firstIdentifierOf(Neighbor.class)
                .child(AfiSafis.class));
        List<String> afiSafis = new ArrayList<>();
        if (optionalAfiSafis.isPresent()) {
            afiSafis = optionalAfiSafis.get()
                    .getAfiSafi()
                    .stream()
                    .map(afiSafi -> GlobalAfiSafiReader.transformAfiToString(afiSafi.getAfiSafiName()))
                    .collect(Collectors.toList());
        }
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);

        blockingWriteAndRead(fT(NEIGHBOR_GLOBAL,
                "as", g.getConfig()
                        .getAs()
                        .getValue(),
                "instance", instName,
                "vrf", nwInsName,
                "address", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue()),
                "afiSafis", afiSafis,
                "enabled", dataAfter.isEnabled(), //chunk can process only getters
                "config", dataAfter,
                "oldConfig", dataBefore),
                cli, id, dataAfter);
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> id, Config config, WriteContext writeContext)
            throws WriteFailedException {
        Optional<Bgp> bgpOptional = writeContext.readAfter(RWUtils.cutId(id, Bgp.class));
        if (!bgpOptional.isPresent()) {
            return;
        }
        final Global g = bgpOptional.get()
                .getGlobal();
        final String instName = GlobalConfigWriter.getProtoInstanceName(id);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(id);
        blockingDeleteAndRead(cli, id,
                f("router bgp %s %s %s", g.getConfig()
                        .getAs()
                        .getValue(), instName, nwInsName),
                f("no neighbor %s", new String(id.firstKeyOf(Neighbor.class)
                        .getNeighborAddress()
                        .getValue())),
                "root");
    }
}

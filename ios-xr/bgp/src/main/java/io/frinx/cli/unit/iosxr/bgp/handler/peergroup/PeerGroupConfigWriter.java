/*
 * Copyright © 2019 Frinx and others.
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

package io.frinx.cli.unit.iosxr.bgp.handler.peergroup;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalConfigWriter;
import io.frinx.cli.unit.utils.CliWriter;
import org.jetbrains.annotations.NotNull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.base.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupConfigWriter implements CliWriter<Config> {

    private Cli cli;

    private static final String WRITE_PEER_GROUP = """
            router bgp {$as} {$instance}{$vrf}
            {% if $delete %}no {% endif %}neighbor-group {$groupName}
            root
            """;

    public PeerGroupConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@NotNull InstanceIdentifier<Config> iid, @NotNull Config config,
                                       @NotNull WriteContext context) throws WriteFailedException {
        Long asNumber = PeerGroupListReader.readAsNumberFromContext(iid, context, false);
        final String instName = GlobalConfigWriter.getProtoInstanceName(iid);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(iid);
        String cmd = fT(WRITE_PEER_GROUP,
                "as", asNumber,
                "instance", instName,
                "vrf", nwInsName,
                "groupName", config.getPeerGroupName());
        blockingWriteAndRead(cmd, cli, iid, config);
    }

    @Override
    public void updateCurrentAttributes(@NotNull InstanceIdentifier<Config> iid, @NotNull Config dataBefore,
                                        @NotNull Config dataAfter, @NotNull WriteContext context)
            throws WriteFailedException {
        writeCurrentAttributes(iid, dataAfter, context);
    }

    @Override
    public void deleteCurrentAttributes(@NotNull InstanceIdentifier<Config> iid, @NotNull Config config,
                                        @NotNull WriteContext context)
            throws WriteFailedException {
        Long asNumber = PeerGroupListReader.readAsNumberFromContext(iid, context, true);
        final String instName = GlobalConfigWriter.getProtoInstanceName(iid);
        final String nwInsName = GlobalConfigWriter.resolveVrfWithName(iid);
        String cmd = fT(WRITE_PEER_GROUP,
                "as", asNumber,
                "instance", instName,
                "vrf", nwInsName,
                "delete", true,
                "groupName", config.getPeerGroupName());
        blockingDeleteAndRead(cmd, cli, iid);
    }
}
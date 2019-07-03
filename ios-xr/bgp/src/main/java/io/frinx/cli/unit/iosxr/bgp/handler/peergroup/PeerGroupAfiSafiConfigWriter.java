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
import io.frinx.cli.unit.iosxr.bgp.handler.GlobalAfiSafiReader;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafi;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.afi.safi.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.list.PeerGroup;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class PeerGroupAfiSafiConfigWriter implements CliWriter<Config> {

    static final String WRITE_PEER_GROUP_AFI_SAFI = "router bgp {$as}\n"
            + "neighbor-group {$groupName}\n"
            + "{% if $delete %}no {% endif %}"
            + "address-family {$afiSafi}\n"
            + "root\n";

    private Cli cli;

    public PeerGroupAfiSafiConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> iid, Config config,
                                             WriteContext context) throws WriteFailedException {
        Long asNumber = PeerGroupListReader.readAsNumberFromContext(iid, context, false);
        String groupName = iid.firstKeyOf(PeerGroup.class).getPeerGroupName();
        blockingWriteAndRead(fT(WRITE_PEER_GROUP_AFI_SAFI,
                "as", asNumber,
                "groupName", groupName,
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(iid.firstKeyOf(AfiSafi.class)
                        .getAfiSafiName()),
                "config", config),
                cli, iid, config);
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> iid, Config dataBefore, Config dataAfter,
                                               WriteContext context) throws WriteFailedException {
        writeCurrentAttributes(iid, dataAfter, context);
    }


    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> iid, Config config, WriteContext context)
            throws WriteFailedException {
        Long asNumber = PeerGroupListReader.readAsNumberFromContext(iid, context, true);
        String groupName = iid.firstKeyOf(PeerGroup.class).getPeerGroupName();
        blockingWriteAndRead(fT(WRITE_PEER_GROUP_AFI_SAFI,
                "as", asNumber,
                "groupName", groupName,
                "afiSafi", GlobalAfiSafiReader.transformAfiToString(iid.firstKeyOf(AfiSafi.class)
                        .getAfiSafiName()),
                "config", config,
                "delete", true),
                cli, iid, config);
    }
}



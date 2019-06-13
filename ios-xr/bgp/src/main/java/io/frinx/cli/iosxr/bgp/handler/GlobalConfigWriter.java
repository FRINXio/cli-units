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

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> id, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final String instName = getProtoInstanceName(id);
        final String nwInsName = resolveVrfWithName(id);

        blockingWriteAndRead(cli, id, data,
                f("router bgp %s %s %s", data.getAs()
                        .getValue(), instName, nwInsName),
                data.getRouterId() != null ? f("bgp router-id %s", data.getRouterId()
                        .getValue()) : "no bgp router-id",
                "root");
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        final String protName = id.firstKeyOf(Protocol.class)
                .getName();
        Preconditions.checkArgument(dataBefore.getAs()
                        .equals(dataAfter.getAs()),
                "Cannot update AS number. Only one BGP instance in instance '{}' is allowed.", protName);
        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> id, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final String instName = getProtoInstanceName(id);
        final String nwInsName = resolveVrfWithName(id);
        blockingDeleteAndRead(cli, id,
                f("no router bgp %s %s %s", data.getAs()
                        .getValue(), instName, nwInsName));
    }

    public static String getProtoInstanceName(InstanceIdentifier<?> id) {
        return NetworInstance.DEFAULT_NETWORK_NAME.equals(id.firstKeyOf(Protocol.class)
                .getName()) ? "" :
                "instance " + id.firstKeyOf(Protocol.class)
                        .getName();
    }


    @VisibleForTesting
    public static String resolveVrfWithName(InstanceIdentifier<?> iid) {
        String vrfId = iid.firstKeyOf(NetworkInstance.class).getName();

        String nwInsName = NetworInstance.DEFAULT_NETWORK_NAME.equals(vrfId)
                ?
                "" : " vrf " + vrfId;
        return nwInsName;
    }
}

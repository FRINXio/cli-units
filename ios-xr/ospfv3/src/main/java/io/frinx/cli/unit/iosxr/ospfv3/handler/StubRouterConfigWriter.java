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

package io.frinx.cli.unit.iosxr.ospfv3.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv3.rev180817.ospfv3.global.structural.global.config.stub.router.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class StubRouterConfigWriter implements CliWriter<Config> {

    private final Cli cli;

    public StubRouterConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(InstanceIdentifier<Config> iid, Config data,
                                              WriteContext context) throws WriteFailedException {
        final String processName = iid.firstKeyOf(Protocol.class).getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);
        blockingWriteAndRead(cli, iid, data,
                f("router ospfv3 %s %s", processName, nwInsName),
                "stub-router router-lsa max-metric",
                data.isAlways() ? "always" : "no always",
                "root");
    }

    @Override
    public void updateCurrentAttributes(InstanceIdentifier<Config> iid, Config dataBefore,
                                               Config dataAfter, WriteContext context) throws
            WriteFailedException {
        final String processName = iid.firstKeyOf(Protocol.class).getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);

        blockingWriteAndRead(cli, iid, dataAfter,
                f("router ospfv3 %s %s", processName, nwInsName),
                "stub-router router-lsa max-metric",
                dataAfter.isAlways() ? "always" : "no always",
                "root");
    }

    @Override
    public void deleteCurrentAttributes(InstanceIdentifier<Config> iid, Config data,
                                               WriteContext context) throws WriteFailedException {
        final String processName = iid.firstKeyOf(Protocol.class).getName();
        final String nwInsName = OspfV3ProtocolReader.resolveVrfWithName(iid);
        blockingWriteAndRead(cli, iid, data,
                f("router ospfv3 %s %s", processName, nwInsName),
                "no stub-router router-lsa max-metric",
                "root");
    }
}

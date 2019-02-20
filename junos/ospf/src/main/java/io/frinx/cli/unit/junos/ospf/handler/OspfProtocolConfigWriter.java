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

package io.frinx.cli.unit.junos.ospf.handler;

import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.ospf.OspfWriter;
import io.frinx.cli.io.Cli;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolConfigWriter implements OspfWriter<Config> {

    private Cli cli;

    public OspfProtocolConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException {
        ProtocolConfAug augData = data.getAugmentation(ProtocolConfAug.class);
        if (augData != null && augData.getExportPolicy() != null) {
            String cmd = f("set%s protocols ospf",
                    OspfProtocolReader.resolveVrfWithName(id),
                    augData.getExportPolicy());

            blockingWriteAndRead(cli, id, data, f("%s export %s", cmd, augData.getExportPolicy()));
        }
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) throws WriteFailedException {
        ProtocolConfAug augAftData = dataAfter.getAugmentation(ProtocolConfAug.class);
        ProtocolConfAug augBfrData = dataAfter.getAugmentation(ProtocolConfAug.class);

        if (augAftData != null && augAftData.getExportPolicy() != null) {
            String cmd = f("set%s protocols ospf",
                    OspfProtocolReader.resolveVrfWithName(id),
                    augAftData.getExportPolicy());

            blockingWriteAndRead(cli, id, dataAfter, f("%s export %s", cmd, augAftData.getExportPolicy()));
        } else if (augBfrData != null && augBfrData.getExportPolicy() != null) {
            String cmd = f("delete%s protocols ospf",
                    OspfProtocolReader.resolveVrfWithName(id),
                    augBfrData.getExportPolicy());

            blockingWriteAndRead(cli, id, augBfrData, f("%s export %s", cmd, augBfrData.getExportPolicy()));
        }
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException {
        final String vrfName = OspfProtocolReader.resolveVrfWithName(id);
        blockingWriteAndRead(cli, id, data,
                f("delete%s protocols ospf", vrfName));
    }
}

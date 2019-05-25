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
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriterFormatter;
import io.frinx.translate.unit.commons.handler.spi.ChecksMap;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.protocol.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.extension.rev190117.ProtocolConfAug;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OspfProtocolConfigWriter implements CliWriterFormatter<Config>, CompositeWriter.Child<Config> {

    private Cli cli;

    public OspfProtocolConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                 @Nonnull Config data,
                                                 @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, false)) {
            return false;
        }

        writeExportPolicy(id, data);
        return true;
    }

    @Override
    public boolean updateCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull Config dataAfter,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, false)
                || isEqualPolicy(dataBefore, dataAfter)) {
            return false;
        }

        // This is correct procedure here, if we register multiple values
        // in export-policy (this type is leaf-list), Junos also remembers their registering order.
        // So, when we update export-policy, we should delete all previous policies
        // and regiester all following policies rather than just addding or deleting differences.
        deleteExportPolicy(id, dataBefore);
        writeExportPolicy(id, dataAfter);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(@Nonnull InstanceIdentifier<Config> id,
                                                  @Nonnull Config dataBefore,
                                                  @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (!ChecksMap.PathCheck.Protocol.OSPF.canProcess(id, writeContext, true)) {
            return false;
        }

        final String vrfName = OspfProtocolReader.resolveVrfWithName(id);
        blockingWriteAndRead(cli, id, dataBefore,
                f("delete%s protocols ospf", vrfName));
        return true;
    }

    private void writeExportPolicy(
        InstanceIdentifier<Config> id,
        Config data) throws WriteFailedException {

        if (data != null && data.getAugmentation(ProtocolConfAug.class) != null
                && data.getAugmentation(ProtocolConfAug.class).getExportPolicy() != null) {
            String cmd = f("set%s protocols ospf", OspfProtocolReader.resolveVrfWithName(id));

            for (String policy : data.getAugmentation(ProtocolConfAug.class).getExportPolicy()) {
                blockingWriteAndRead(cli, id, data, f("%s export %s", cmd, policy));
            }
        }
    }

    private void deleteExportPolicy(
        InstanceIdentifier<Config> id,
        Config data) throws WriteFailedException {

        if (data != null && data.getAugmentation(ProtocolConfAug.class) != null
                && data.getAugmentation(ProtocolConfAug.class).getExportPolicy() != null) {
            String cmd = f("delete%s protocols ospf", OspfProtocolReader.resolveVrfWithName(id));

            for (String policy : data.getAugmentation(ProtocolConfAug.class).getExportPolicy()) {
                blockingDeleteAndRead(cli, id, f("%s export %s", cmd, policy));
            }
        }
    }

    private static boolean isEqualPolicy(Config beforeData, Config afterData) {
        List<String> bfrList = Collections.emptyList();
        List<String> aftList = Collections.emptyList();

        if (beforeData != null && beforeData.getAugmentation(ProtocolConfAug.class) != null
                && beforeData.getAugmentation(ProtocolConfAug.class).getExportPolicy() != null) {
            bfrList = beforeData.getAugmentation(ProtocolConfAug.class).getExportPolicy();
        }
        if (afterData != null && afterData.getAugmentation(ProtocolConfAug.class) != null
                && afterData.getAugmentation(ProtocolConfAug.class).getExportPolicy() != null) {
            aftList = afterData.getAugmentation(ProtocolConfAug.class).getExportPolicy();
        }

        return bfrList.equals(aftList);
    }

}

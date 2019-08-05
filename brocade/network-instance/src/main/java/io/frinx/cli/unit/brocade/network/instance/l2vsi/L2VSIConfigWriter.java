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

package io.frinx.cli.unit.brocade.network.instance.l2vsi;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.ifc.L2VSIInterfaceReader;
import io.frinx.cli.unit.utils.CliWriter;
import io.frinx.openconfig.openconfig.network.instance.IIDs;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConnectionPoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.ConnectionPoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.Endpoints;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.connection.points.connection.point.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.types.rev170228.REMOTE;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.utils.IidUtils;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSIConfigWriter implements CompositeWriter.Child<Config>, CliWriter<Config> {

    static String VPLS_MTU = "configure terminal\n"
            + "router mpls\n"
            + "vpls {$data.name} {$vccid}\n"
            + "{$data|update(mtu,vpls-mtu `$data.mtu`\n,no vpls-mtu `$before.mtu`\n)}"
            + "end";

    static String DELETE_VPLS_MTU = "{% if ($data.mtu) %}"
            + "configure terminal\n"
            + "router mpls\n"
            + "vpls {$data.name} {$vccid}\n"
            + "no vpls-mtu {$data.mtu}\n"
            + "end"
            + "{% endif %}";

    private final Cli cli;

    public L2VSIConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(InstanceIdentifier<Config> id,
                                                 Config config,
                                                 WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {
        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(id, writeContext, false)) {
            return false;
        }
        writeCommand(id, null, writeContext, config);
        return true;
    }

    private void writeCommand(InstanceIdentifier<Config> id, Config before,
                                WriteContext writeContext, Config data)
            throws WriteFailedException.CreateFailedException {
        Optional<Long> vccidOp = getVccid(id, writeContext, true);

        if (vccidOp.isPresent()) {
            blockingWriteAndRead(getCommand(VPLS_MTU, before, data, vccidOp.get()),
                    cli, id, data);
        }
    }

    @VisibleForTesting
    String getCommand(String template, Config before, Config config, Long vccid) {
        return fT(template, "before", before, "data", config, "vccid", vccid);
    }

    private Optional<Long> getVccid(InstanceIdentifier<Config> id, WriteContext writeContext, boolean write) {
        NetworkInstance networkInstance = write
                ? writeContext.readAfter(id.firstIdentifierOf(NetworkInstance.class)).get()
                : writeContext.readBefore(id.firstIdentifierOf(NetworkInstance.class)).get();
        return Optional.of(networkInstance)
                .map(NetworkInstance::getConnectionPoints)
                .map(ConnectionPoints::getConnectionPoint)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ConnectionPoint::getEndpoints)
                .filter(Objects::nonNull)
                .map(Endpoints::getEndpoint)
                .filter(Objects::nonNull)
                .flatMap(e -> e.stream().filter(ep -> ep.getConfig().getType() == REMOTE.class))
                .map(Endpoint::getRemote)
                .filter(Objects::nonNull)
                .map(r -> r.getConfig().getVirtualCircuitIdentifier())
                .findFirst();
    }

    @Override
    public boolean updateCurrentAttributesWResult(InstanceIdentifier<Config> id,
                                                  Config dataBefore, Config dataAfter,
                                                  WriteContext writeContext)
            throws WriteFailedException {
        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(id, writeContext, false)) {
            return false;
        }
        writeCommand(id, dataBefore, writeContext, dataAfter);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(InstanceIdentifier<Config> id,
                                                  Config config,
                                                  WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        if (!L2VSIInterfaceReader.L2VSI_CHECK.canProcess(id, writeContext, true)) {
            return false;
        }

        if (!writeContext.readAfter(IidUtils.createIid(IIDs.NE_NE_CONNECTIONPOINTS,
                id.firstKeyOf(NetworkInstance.class))).isPresent()) {
            return true;
        }

        deleteCommand(id, writeContext, config);
        return true;
    }

    private void deleteCommand(InstanceIdentifier<Config> id, WriteContext writeContext, Config before)
            throws WriteFailedException.DeleteFailedException {
        Optional<Long> vccidOp = getVccid(id, writeContext, false);

        if (vccidOp.isPresent()) {
            blockingDeleteAndRead(getCommand(DELETE_VPLS_MTU, null, before, vccidOp.get()), cli, id);
        }
    }
}


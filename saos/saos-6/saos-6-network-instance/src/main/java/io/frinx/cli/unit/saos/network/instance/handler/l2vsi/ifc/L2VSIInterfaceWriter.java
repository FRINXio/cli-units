/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.x5.template.Chunk;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.network.instance.handler.l2vsi.L2VSIReader;
import io.frinx.cli.unit.utils.CliListWriter;
import io.frinx.translate.unit.commons.handler.spi.CompositeWriter;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class L2VSIInterfaceWriter implements CliListWriter<Interface, InterfaceKey>, CompositeWriter.Child<Interface> {

    private static final String TEMPLATE =
                "{% if ($delete) %}virtual-switch ethernet remove vs {$vsi_ni_name} port {$vsi_ni_if_name}\n"
                + "configuration save{% endif %}"
                + "{% if ($add) %}virtual-switch ethernet add vs {$vsi_ni_name} port {$vsi_ni_if_name}\n"
                + "port set port {$vsi_ni_if_name} untagged-ctrl-vs {$vsi_ni_name}\n"
                + "port set port {$vsi_ni_if_name} untagged-data-vs {$vsi_ni_name}\n"
                + "configuration save{% endif %}";

    private Cli cli;

    public L2VSIInterfaceWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public boolean writeCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                 Interface anInterface,
                                                 WriteContext writeContext) throws WriteFailedException {
        if (!L2VSIReader.basicCheck_L2VSI.canProcess(instanceIdentifier, writeContext, false)) {
            return false;
        }
        checkWriteInterfaceData(instanceIdentifier, anInterface, writeContext);

        writeCurrentAttributesTesting(instanceIdentifier, anInterface);
        return true;
    }

    @VisibleForTesting
    void writeCurrentAttributesTesting(InstanceIdentifier<Interface> instanceIdentifier,
                                       Interface anInterface) throws WriteFailedException {
        final String niName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        String ifName = anInterface.getId();

        blockingWriteAndRead(cli, instanceIdentifier, anInterface,
                getTemplate(niName, ifName, true, false));
    }

    private String getTemplate(String niName, String ifName, Boolean add, Boolean delete) {
        return fT(TEMPLATE, "vsi_ni_name", niName, "vsi_ni_if_name", ifName,
                "add", add ? Chunk.TRUE : null, "delete", delete ? Chunk.TRUE : null);
    }

    private void checkWriteInterfaceData(InstanceIdentifier<Interface> instanceIdentifier,
                                         Interface anInterface,
                                         WriteContext writeContext) {
        boolean portIdPresentVsNot = (anInterface.getId() != null && !anInterface.getId().isEmpty())
                && writeContext.readAfter(instanceIdentifier.firstIdentifierOf(NetworkInstance.class)).isPresent();
        Preconditions.checkArgument(portIdPresentVsNot, "Cannot create interface without virtual-switch");

        String interfaceId = Optional.ofNullable(anInterface.getId()).orElse(null);
        Preconditions.checkNotNull(interfaceId, "Missing interface id");

        String interfaceConfigId = Optional.ofNullable(anInterface.getConfig())
                .map(Config::getId)
                .orElse(null);
        Preconditions.checkNotNull(interfaceConfigId, "Missing interface id as ID in config");

        String interfaceConfigInterfaceId = Optional.ofNullable(anInterface.getConfig())
                .map(Config::getInterface)
                .orElse(null);

        Preconditions.checkNotNull(interfaceConfigInterfaceId,
                "Missing interface id as Interface in config");

        Preconditions.checkArgument(Objects.equals(interfaceId, interfaceConfigId)
                        && Objects.equals(interfaceConfigInterfaceId, interfaceConfigId),
                "Interface Id and Id with Interface from interface config must be the same");
    }

    private void checkDeleteInterfaceData(InstanceIdentifier<Interface> instanceIdentifier,
                                          Interface anInterface,
                                          WriteContext writeContext) {
        com.google.common.base.Optional<NetworkInstance> networkInstance = writeContext.readBefore(instanceIdentifier
                .firstIdentifierOf(NetworkInstance.class));

        boolean portIdPresentVsNot = (anInterface.getId() != null && !anInterface.getId().isEmpty())
                && networkInstance.isPresent();

        Preconditions.checkArgument(portIdPresentVsNot, "Cannot delete interface without virtual-switch");

        String interfaceId = Optional.ofNullable(anInterface.getId()).orElse(null);
        Preconditions.checkNotNull(interfaceId, "Missing interface");

        String interfaceConfigId = Optional.ofNullable(anInterface.getConfig())
                .map(Config::getId)
                .orElse(null);
        Preconditions.checkNotNull(interfaceConfigId, "Missing interface id as ID in config");

        String interfaceConfigInterfaceId = Optional.ofNullable(anInterface.getConfig())
                .map(Config::getInterface)
                .orElse(null);

        Preconditions.checkNotNull(interfaceConfigInterfaceId,
                "Missing interface id as Interface in config");

        Preconditions.checkArgument(Objects.equals(interfaceId, interfaceConfigId)
                        && Objects.equals(interfaceConfigInterfaceId, interfaceConfigId),
                "Interface Id and Id with Interface from interface config must be the same");
    }

    @Override
    public boolean updateCurrentAttributesWResult(InstanceIdentifier<Interface> id,
                                                  Interface dataBefore,
                                                  Interface dataAfter,
                                                  WriteContext writeContext) {
        checkWriteInterfaceData(id, dataAfter, writeContext);
        return true;
    }

    @Override
    public boolean deleteCurrentAttributesWResult(InstanceIdentifier<Interface> instanceIdentifier,
                                                  Interface anInterface,
                                                  WriteContext writeContext) throws WriteFailedException {
        checkDeleteInterfaceData(instanceIdentifier, anInterface, writeContext);
        deleteCurrentAttributesTesting(instanceIdentifier, anInterface);

        return true;
    }

    @VisibleForTesting
    void deleteCurrentAttributesTesting(InstanceIdentifier<Interface> instanceIdentifier,
                                        Interface anInterface) throws WriteFailedException {
        NetworkInstanceKey networkInstanceKey = instanceIdentifier.firstKeyOf(NetworkInstance.class);
        blockingDeleteAndRead(cli, instanceIdentifier, getTemplate(networkInstanceKey.getName(), anInterface.getId(),
                false, true));
    }
}
